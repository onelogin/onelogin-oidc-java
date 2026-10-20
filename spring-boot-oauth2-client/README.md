# OneLogin OpenId Connect Spring Security OAuth2 Client Sample
A minimal Spring Boot 4 app that signs users in with OneLogin using the OpenId Connect Authorization Code flow. It uses Spring Security's built-in OAuth2 Client support (`oauth2Login()`), which replaced the `@EnableOAuth2Sso` approach in [spring-boot-app](../spring-boot-app). The libraries that sample depends on are end of life.

The app has three parts:
- a public home page with a **Log in with OneLogin** link
- a protected `/profile` page that lists the claims in the ID token OneLogin issued
- a **Log out** button that ends both the app session and the OneLogin session

## Prerequisites
- Java 17 or later. The included Maven wrapper (`./mvnw`) downloads Maven for you.
- A OneLogin account. If you don't have one you can [create a free developer account here](https://www.onelogin.com/developer-signup).
- An OpenId Connect app in your OneLogin Admin portal. [You can read more about how to do that here](https://developers.onelogin.com/openid-connect/connect-to-onelogin).

## Configure the OneLogin app
In your OpenId Connect app in OneLogin:

- On the **Configuration** tab:
  - add `http://localhost:8080/login/oauth2/code/onelogin` as a **Redirect URI**. Spring Security handles the login callback on `/login/oauth2/code/{registrationId}`, and this sample's registration ID is `onelogin`.
  - set the **Post Logout Redirect URI** to `http://localhost:8080/`, so that OneLogin can send the browser back to the app after logout.
- On the **SSO** tab, set the Token Endpoint **Authentication Method** to **Basic**, which is what Spring Security uses by default. Copy the **Client ID** and **Client Secret**.
- Give the users who will sign in access to the app, for example through a role on the **Access** tab.

## Run the sample
Set the issuer and client credentials, then start the app:

```sh
export ONELOGIN_OIDC_ISSUER=https://<subdomain>.onelogin.com/oidc/2
export ONELOGIN_OIDC_CLIENT_ID=<client id>
export ONELOGIN_OIDC_CLIENT_SECRET=<client secret>
./mvnw spring-boot:run
```

Replace `<subdomain>` with your OneLogin subdomain. Open http://localhost:8080 and click **Log in with OneLogin**.

## How it works
- [`application.yml`](src/main/resources/application.yml) registers an OAuth2 client called `onelogin`. Because it sets `issuer-uri`, Spring Security reads OneLogin's discovery document (`<issuer>/.well-known/openid-configuration`) at startup. The authorization, token, userinfo, JWKS and logout endpoints all come from that document, so you don't need to configure them.
- [`SecurityConfig`](src/main/java/com/onelogin/example/oidc/SecurityConfig.java) leaves `/` public, requires login everywhere else, and turns on `oauth2Login()`. On logout, `OidcClientInitiatedLogoutSuccessHandler` redirects the browser to OneLogin's `end_session_endpoint`, so the OneLogin session ends too.
- [`HomeController`](src/main/java/com/onelogin/example/oidc/HomeController.java) gets the signed-in user as an `OidcUser`. Spring Security has already validated the ID token's signature, issuer, audience, expiry and nonce, and fetched the userinfo claims.
- Spring Security sends a PKCE `code_challenge` with every authorization request.

## Other token endpoint authentication methods
If you set the OneLogin app's Token Endpoint **Authentication Method** to something other than Basic, tell Spring Security to match it by adding `client-authentication-method` under `registration.onelogin` in `application.yml`:

| OneLogin Authentication Method | `client-authentication-method` | Client secret |
| --- | --- | --- |
| Basic | `client_secret_basic` (the default, so you don't need to set it) | required |
| POST | `client_secret_post` | required |
| None (PKCE) | `none` | remove the `client-secret` line |

## Troubleshooting
- **The app fails to start with `Not enough variable values available to expand 'ONELOGIN_OIDC_ISSUER'`.** The environment variables above aren't set in the shell that started the app.
- **OneLogin shows a redirect URI error.** The app's Redirect URI in OneLogin must be exactly `http://localhost:8080/login/oauth2/code/onelogin`.
- **After you sign in at OneLogin, the app shows "Invalid credentials" at `/login?error`.** The app could not exchange the authorization code for tokens. The log shows `[invalid_token_response]` with `401 Unauthorized` and `invalid_client`. Check the client secret, and check that the OneLogin Authentication Method matches `client-authentication-method` (see the table above).
