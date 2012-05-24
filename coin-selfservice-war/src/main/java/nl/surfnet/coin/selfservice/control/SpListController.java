/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.selfservice.control;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import nl.surfnet.coin.selfservice.domain.ServiceProvider;
import nl.surfnet.coin.selfservice.service.ProviderService;

@Controller
public class SpListController {


  @Resource(name="providerService")
  private ProviderService providerService;

  @RequestMapping(value="/linked-sps")
  // TODO: replace idp parameter with security-context-provided one.
  public ModelAndView listAllSps(@RequestParam(value="idp", defaultValue="idpentity1") String idpId) {
    Map<String, Object> m = new HashMap<String, Object>();

    m.put("sps", providerService.getLinkedServiceProviders(idpId));

    return new ModelAndView("linked-sps", m);
  }

  @RequestMapping(value="/sp/detail.shtml")
  public ModelAndView spDetail(@RequestParam String spEntityId) {
    Map<String, Object> m = new HashMap<String, Object>();
    final ServiceProvider sp = providerService.getServiceProvider(spEntityId);
    m.put("sp", sp);
    return new ModelAndView("sp-detail", m);
  }
}
