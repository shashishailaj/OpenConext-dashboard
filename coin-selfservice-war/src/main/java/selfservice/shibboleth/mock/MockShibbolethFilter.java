package selfservice.shibboleth.mock;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import static selfservice.api.rest.Constants.HTTP_X_IDP_ENTITY_ID;

public class MockShibbolethFilter extends GenericFilterBean {

  private static class SetHeader extends HttpServletRequestWrapper {

    private final HashMap<String, String> headers;

    public SetHeader(HttpServletRequest request) {
      super(request);
      this.headers = new HashMap<>();
    }

    public void setHeader(String name, String value) {
      this.headers.put(name, value);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      @SuppressWarnings("unchecked")
      List<String> names = Collections.list(super.getHeaderNames());
      names.addAll(headers.keySet());
      return Collections.enumeration(names);
    }

    @Override
    public String getHeader(String name) {
      if (headers.containsKey(name)) {
        return headers.get(name);
      }
      return super.getHeader(name);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String userId = request.getParameter("mockUser");//"admin";
    if (userId == null) {
      userId = (String) req.getSession().getAttribute("mockShibbolethUser");
    }
    if (userId == null) {
      String redirectTo = request.getParameter("redirectTo");
      String login = IOUtils.toString(new ClassPathResource("mockLogin.html").getInputStream());
      if (redirectTo != null) {
        login = login.replaceFirst("@@redirectTo@@", redirectTo);
      }
      response.getWriter().write(login);
    } else {
      req.getSession(true).setAttribute("mockShibbolethUser", userId);
      SetHeader wrapper = new SetHeader(req);
      wrapper.setHeader("name-id", userId);
      wrapper.setHeader("Shib-uid", userId);
      String idp = "https://idp.surfnet.nl";
      wrapper.setHeader("Shib-Authenticating-Authority", idp);
      wrapper.setHeader("Shib-displayName", "Ben Vonk");
      wrapper.setHeader(HTTP_X_IDP_ENTITY_ID, idp);
      chain.doFilter(wrapper, response);
    }
  }

}