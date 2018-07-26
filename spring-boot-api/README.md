# OneLogin OpenId Connect Spring Boot Sample
This is a bare bones Spring Boot app that has been modified to support the OpenId Connect Authorization Code flow via OneLogin.

## Prerequisites
This sample uses Spring which requires [Java SDK 1.8+](https://www.java.com/) and [Apache Maven 3.2+](https://maven.apache.org/).

You will also need a OneLogin account. If you don't have one you can [create a free developer account here](https://www.onelogin.com/developer-signup).

Most importantly you will need to create an OpenId Connect app in your OneLogin Admin portal. [You can read more about how to do that here](https://developers.onelogin.com/openid-connect/connect-to-onelogin).

## Setup
You can pull the source of this sample and change the client id, secret, &amp; subdomain in `app/src/main/resources/application.yml` or alternately follow these steps.

### Step 1.
Create a new directory for your app.

```sh
mkdir spring-boot-api && cd spring-boot-api
```

Then download a Spring Boot starter web app with security enabled.
```sh
curl start.spring.io/starter.tgz -d dependencies=web -d dependencies=security | tar -zxvf -
```

### Step 2.
Add the following dependencies to `pom.xml`.

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2</artifactId>
</dependency>

<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>jwks-rsa</artifactId>
    <version>0.3.0</version>
</dependency>
```

### Step 3.
Rename `app/src/main/resources/application.properties` to `app/src/main/resources/application.yml` and then paste in the following configuration.

```yml
spring:
  security:
    oauth2:
      client:
        registration:
          onelogin:
            client-name: OneLogin
            client-id: your-onelogin-oidc-app-client-id
            client-secret: your-onelogin-oidc-app-client-secret
            provider: onelogin
            scope: openid profile
            client-authentication-method: basic
            authorization-grant-type: authorization_code
            redirect-uri-template: http://localhost:8080/login/oauth2/code/onelogin
        provider:
          onelogin:
            authorization-uri: https://your-sub-domain.onelogin.com/oidc/auth
            token-uri: https://your-sub-domain.onelogin.com/oidc/token
            user-info-uri: https://your-sub-domain.onelogin.com/oidc/me
            jwk-set-uri: https://your-sub-domain.onelogin.com/oidc/certs
            user-name-attribute: sub
```

Make sure you replace `your-onelogin-oidc-app-client-id` and `your-onelogin-oidc-app-client-secret` with the values provided when you created your OpenId Connect app via the OneLogin portal.

Change `your-sub-domain` to match the subdomain of your OneLogin portal.

The `redirect-uri-template` should match the redirect uri that you have specified in your OneLogin OpenId Connect app configuration.

### Step 4.
Create a new file called `HomeController.java` in the `src/main/java/com/example/demo` directory and paste in the following.

```java
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
```

## Run the sample
Run the sample from your terminal with

```sh
mvn spring-boot:run
```

Your app will be available at http://localhost:8080 and you should see a **OneLogin** link. Click on the link to start the login process.