package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@RestController
public class HomeController {

  @Autowired
  private OAuth2AuthorizedClientService authorizedClientService;

  @GetMapping("/")
  public String home(Principal user, OAuth2AuthenticationToken authentication) {

    // Success. User has been authenticated
    System.out.print("OneLogin UserId: " + user.getName());

    // Get the client for the authorized user
    OAuth2AuthorizedClient client = authorizedClientService
    .loadAuthorizedClient(
      authentication.getAuthorizedClientRegistrationId(),
        authentication.getName());

    // The endpoint for getting user info from OneLogin
    String userInfoEndpointUri = client.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUri();

    // Make a request to get the user info
    Map userAttributes = Collections.emptyMap();
    if (!StringUtils.isEmpty(userInfoEndpointUri)) {

      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken()
        .getTokenValue());
      HttpEntity<String> entity = new HttpEntity<String>("", headers);
      ResponseEntity<Map> response = restTemplate.exchange(userInfoEndpointUri, HttpMethod.GET, entity, Map.class);

      userAttributes = response.getBody();
    }

    return "Success. " + userAttributes.toString();
  }
}

