package com.onelogin.example.oidc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.test.web.servlet.MockMvc;

// The application.yml registration uses issuer-uri, which fetches OneLogin's discovery document
// at startup. These tests replace it with a fixed registration so they run without a network.
@SpringBootTest
@AutoConfigureMockMvc
class OidcClientApplicationTests {

	private static final String ISSUER = "https://example.onelogin.com/oidc/2";

	private static final ClientRegistration ONELOGIN = ClientRegistration.withRegistrationId("onelogin")
		.clientId("test-client-id")
		.clientSecret("test-client-secret")
		.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
		.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
		.scope("openid", "profile", "email")
		.authorizationUri(ISSUER + "/auth")
		.tokenUri(ISSUER + "/token")
		.jwkSetUri(ISSUER + "/certs")
		.userInfoUri(ISSUER + "/me")
		.userNameAttributeName(IdTokenClaimNames.SUB)
		.issuerUri(ISSUER)
		.providerConfigurationMetadata(Map.of("end_session_endpoint", ISSUER + "/logout"))
		.build();

	@TestConfiguration
	static class ClientRegistrationConfig {

		@Bean
		ClientRegistrationRepository clientRegistrationRepository() {
			return new InMemoryClientRegistrationRepository(ONELOGIN);
		}

	}

	@Autowired
	private MockMvc mvc;

	@Test
	void homePageIsPublic() throws Exception {
		this.mvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Log in with OneLogin")));
	}

	@Test
	void profileRedirectsAnonymousUsersToLogin() throws Exception {
		this.mvc.perform(get("/profile"))
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/oauth2/authorization/onelogin"));
	}

	@Test
	void loginRedirectsToOneLoginWithPkce() throws Exception {
		this.mvc.perform(get("/oauth2/authorization/onelogin"))
			.andExpect(status().isFound())
			.andExpect(header().string("Location", startsWith(ISSUER + "/auth?")))
			.andExpect(header().string("Location", containsString("client_id=test-client-id")))
			.andExpect(header().string("Location",
					containsString("redirect_uri=http://localhost/login/oauth2/code/onelogin")))
			.andExpect(header().string("Location", containsString("code_challenge_method=S256")));
	}

	@Test
	void profileShowsIdTokenClaims() throws Exception {
		this.mvc.perform(get("/profile").with(oidcLogin().clientRegistration(ONELOGIN)
				.idToken((token) -> token.subject("12345").claim("name", "Ada Lovelace"))))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Ada Lovelace")))
			.andExpect(content().string(containsString("12345")));
	}

	@Test
	void logoutRedirectsToOneLoginEndSessionEndpoint() throws Exception {
		this.mvc.perform(post("/logout").with(csrf()).with(oidcLogin().clientRegistration(ONELOGIN)))
			.andExpect(status().isFound())
			.andExpect(header().string("Location", startsWith(ISSUER + "/logout?id_token_hint=")))
			.andExpect(header().string("Location",
					containsString("post_logout_redirect_uri=http://localhost/")));
	}

}
