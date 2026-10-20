# OneLogin OpenId Connect Spring Boot Sample
This is a bare bones Spring Boot app that has been modified to support the OpenId Connect Authorization Code flow via OneLogin.

> This sample uses Spring Boot 2.2 and `spring-security-oauth2-autoconfigure`, which are both end of life. For a new app, use [spring-boot-oauth2-client](../spring-boot-oauth2-client), which is built on Spring Security's current OAuth2 Client support.

## Prerequisites
This sample uses Spring which requires [Java SDK 1.11+](https://www.java.com/) and [Apache Maven 3.6+](https://maven.apache.org/).

You will also need a OneLogin account. If you don't have one you can [create a free developer account here](https://www.onelogin.com/developer-signup).

Most importantly you will need to create an OpenId Connect app in your OneLogin Admin portal. [You can read more about how to do that here](https://developers.onelogin.com/openid-connect/connect-to-onelogin).

## Configure the OneLogin app
In your OpenId Connect app in OneLogin:

- On the **Configuration** tab, add `http://localhost:8081/login` as a **Redirect URI**. `@EnableOAuth2Sso` handles the login callback on `/login`, so OneLogin rejects the login with a redirect URI mismatch if this is missing.
- On the **SSO** tab, set the Token Endpoint **Authentication Method** to **POST**. This matches `clientAuthenticationScheme: form` in `application.yml`. If you'd rather use **Basic**, remove that line instead.

## Setup
You can pull the source of this sample and change the subdomain, clientId and clientSecret in `src/main/resources/application.yml` or alternately follow these steps.

The steps below target Spring Boot 2.x and the `spring-security-oauth2-autoconfigure` library (`@EnableOAuth2Sso`). [start.spring.io](https://start.spring.io) now only generates newer Spring Boot versions, which that library doesn't support, so pulling the source is the simplest way to run this sample.

### Step 1.
Create a new directory for your app.

```sh
mkdir spring-boot-app && cd spring-boot-app
```

Then download a Spring Boot starter web app with security enabled.
```sh
curl start.spring.io/starter.tgz -d dependencies=web -d dependencies=security | tar -zxvf -
```

### Step 2.
Add the following dependencies to `pom.xml`.

```xml
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.security.oauth.boot</groupId>
			<artifactId>spring-security-oauth2-autoconfigure</artifactId>
			<version>2.1.1.RELEASE</version>
		</dependency>

		<dependency>
			<groupId>org.webjars</groupId>
			<artifactId>jquery</artifactId>
			<version>2.1.1</version>
		</dependency>

		<dependency>
			<groupId>org.webjars</groupId>
			<artifactId>bootstrap</artifactId>
			<version>3.2.0</version>
		</dependency>

		<dependency>
			<groupId>org.webjars</groupId>
			<artifactId>webjars-locator-core</artifactId>
		</dependency>

		<dependency>
		    <groupId>org.webjars</groupId>
		    <artifactId>js-cookie</artifactId>
		    <version>2.1.0</version>
		</dependency>
```

### Step 3.
Rename `src/main/resources/application.properties` to `src/main/resources/application.yml` and then paste in the following configuration.

```yml
security:
  oauth2:
    client:
      clientId: your-onelogin-oidc-app-client-id
      clientSecret: your-onelogin-oidc-app-client-secret
      accessTokenUri: https://<subdomain>.onelogin.com/oidc/2/token
      userAuthorizationUri: https://<subdomain>.onelogin.com/oidc/2/auth
      clientAuthenticationScheme: form
      tokenName: access_token
      authorizedGrantTypes: authorization_code
      scope: openid,profile,email
    resource:
      userInfoUri: https://<subdomain>.onelogin.com/oidc/2/me
server:
  port : 8081
```

Make sure you replace `subdomain`, `your-onelogin-oidc-app-client-id` and `your-onelogin-oidc-app-client-secret` with the values provided when you created your OpenId Connect app via the OneLogin portal.


### Step 4.
Create a new file called `index.html` in the `src/main/resources/static` directory and paste in the following.

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>Demo</title>
    <meta name="description" content=""/>
    <meta name="viewport" content="width=device-width"/>
    <base href="/"/>
    <link rel="stylesheet" type="text/css" href="/webjars/bootstrap/css/bootstrap.min.css"/>
    <script type="text/javascript" src="/webjars/jquery/jquery.min.js"></script>
    <script type="text/javascript" src="/webjars/bootstrap/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/webjars/js-cookie/js.cookie.js"></script>
</head>
<body>
	<h1>Demo</h1>
	<div class="container unauthenticated">
	    Login with OneLogin: <a href="/login">click here</a>
	</div>
	<div class="container authenticated" style="display:none">
	    Logged in as: <span id="user"></span>
    <div>
      <button onClick="logout()" class="btn btn-primary">Logout</button>
    </div>
	</div>
	<script type="text/javascript">
	    $.get("/user", function(data) {
	        $("#user").html(data.userAuthentication.details.name);
	        $(".unauthenticated").hide()
	        $(".authenticated").show()
	    });
	    var logout = function() {
		    $.post("/logout", function() {
		        $("#user").html('');
		        $(".unauthenticated").show();
		        $(".authenticated").hide();
		    })
		    return true;
		}
		$.ajaxSetup({
			beforeSend : function(xhr, settings) {
			  if (settings.type == 'POST' || settings.type == 'PUT'
			      || settings.type == 'DELETE') {
			    if (!(/^http:.*/.test(settings.url) || /^https:.*/
			        .test(settings.url))) {
			      // Only send the token to relative URLs i.e. locally.
			      xhr.setRequestHeader("X-XSRF-TOKEN",
			          Cookies.get('XSRF-TOKEN'));
			    }
			  }
			}
		});
	</script>
</body>
</html>
```

### Step 5.
Open your main java class file and copy paste the following code:

```java
package com.example.demo;

import java.security.Principal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableOAuth2Sso
@RestController
public class DemoApplication extends WebSecurityConfigurerAdapter {

	@RequestMapping("/user")
	public Principal user(Principal principal) {
		return principal;
  }

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.antMatcher("/**")
			.authorizeRequests()
				.antMatchers("/", "/login**", "/webjars/**", "/error**")
				.permitAll()
			.anyRequest()
        .authenticated()
      .and().logout().logoutSuccessUrl("/").permitAll()
      .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
```

## Run the sample
Run the sample from your terminal with

```sh
mvn spring-boot:run
```

Your app will be available at http://localhost:8081 and you should see a **Login with OneLogin** link. Click on the link to start the login process.