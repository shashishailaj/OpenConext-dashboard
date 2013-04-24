package nl.surfnet.coin.selfservice.control.api;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.selfservice.domain.ApiService;
import nl.surfnet.coin.selfservice.domain.Article;
import nl.surfnet.coin.selfservice.domain.CompoundServiceProvider;
import nl.surfnet.coin.selfservice.domain.IdentityProvider;
import nl.surfnet.coin.selfservice.service.IdentityProviderService;
import nl.surfnet.coin.selfservice.service.LmngService;
import nl.surfnet.coin.selfservice.service.impl.CompoundSPService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.surfnet.oaaas.auth.AuthorizationServerFilter;
import org.surfnet.oaaas.conext.SAMLAuthenticatedPrincipal;
import org.surfnet.oaaas.model.VerifyTokenResponse;

@Controller
@RequestMapping(value = "/api/*")
public class ApiController {

  private @Value("${WEB_APPLICATION_CHANNEL}")
  String protocol;
  private @Value("${WEB_APPLICATION_HOST_AND_PORT}")
  String hostAndPort;
  private @Value("${WEB_APPLICATION_CONTEXT_PATH}")
  String contextPath;
  private @Value("${lmngDeepLinkBaseUrl}")
  String lmngDeepLinkBaseUrl;

  @Resource
  private CompoundSPService compoundSPService;
  
  @Resource
  private LmngService lmngService;
  
  @Resource
  private IdentityProviderService idpService;
  
  @Value("${public.api.lmng.guids}")
  private String[] guids;

  @RequestMapping(value = "/public/services.json")
  public @ResponseBody
  List<ApiService> getPublicServices(@RequestParam(value = "lang", defaultValue = "en") String language,
      final HttpServletRequest request) {
    if ((Boolean) (request.getAttribute("lmngActive"))) {
      List<CompoundServiceProvider> csPs = compoundSPService.getAllPublicCSPs();
      List<ApiService> result = buildApiServices(csPs, language);
      
      //add public service from LMNG directly
      for (String guid : guids) {
        Article currentArticle = lmngService.getService(guid);
        ApiService currentPS = new ApiService(currentArticle.getServiceDescriptionNl(), currentArticle.getDetailLogo(), null, true, lmngDeepLinkBaseUrl + guid);
        result.add(currentPS);
      }
      sort(result);
      return result;
    } else {
      throw new RuntimeException("Only allowed in showroom, not in dashboard");
    }
  }

  private String getServiceLogo(CompoundServiceProvider csP) {
    String detailLogo = csP.getDetailLogo();
    if (detailLogo != null) {
      if (detailLogo.startsWith("/")) {
        detailLogo = protocol + "://" + hostAndPort + (StringUtils.hasText(contextPath) ? contextPath : "")
            + detailLogo;
      }
    }
    return detailLogo;
  }

  @RequestMapping(value = "/protected/services.json")
  public @ResponseBody
  List<ApiService> getProtectedServices(@RequestParam(value = "lang", defaultValue = "en") String language,
                                        final HttpServletRequest request) {
    if ((Boolean) (request.getAttribute("lmngActive"))) {
      String ipdEntityId = getIdpEntityIdFromToken(request);
      IdentityProvider identityProvider = idpService.getIdentityProvider(ipdEntityId);
      List<CompoundServiceProvider> csPs = compoundSPService.getCSPsByIdp(identityProvider);
      List<ApiService> result = buildApiServices(csPs, language);
      
      sort(result);
      return result;
    } else {
      throw new RuntimeException("Only allowed in showroom, not in dashboard");
    }
  }

  /**
   * Retrieve IDP Entity ID from the oauth token stored in the request
   * @param request httpServletRequest to look in.
   * @return identityProvider of the principle
   */
  private String getIdpEntityIdFromToken(final HttpServletRequest request) {
    VerifyTokenResponse verifyTokenResponse = (VerifyTokenResponse) request.getAttribute(AuthorizationServerFilter.VERIFY_TOKEN_RESPONSE);
    SAMLAuthenticatedPrincipal principal =  (SAMLAuthenticatedPrincipal) verifyTokenResponse.getPrincipal();
    return principal.getIdentityProvider();
  }

  /**
   * Convert the list of found services to a list of services that can be displayed in the API
   * (either public or private)
   * @param services list of services to convert (compound service providers)
   * @param language language to use in the result
   * @return a list of api services
   */
  private List<ApiService> buildApiServices(List<CompoundServiceProvider> services, String language) {
    List<ApiService> result = new ArrayList<ApiService>();
    boolean isEn = language.equalsIgnoreCase("en");
    for (CompoundServiceProvider csP : services) {
      String crmLink = csP.isArticleAvailable() ? (lmngDeepLinkBaseUrl + csP.getLmngId()) : null;
      result.add(new ApiService(isEn ? csP.getServiceDescriptionEn() : csP.getServiceDescriptionNl(),
          getServiceLogo(csP), csP.getServiceUrl(), csP.isArticleAvailable(), crmLink));
    }
    return result;
  }

}
