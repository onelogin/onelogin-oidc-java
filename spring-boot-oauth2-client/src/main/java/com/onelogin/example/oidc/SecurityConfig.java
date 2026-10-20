package com.onelogin.example.oidc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			ClientRegistrationRepository clientRegistrationRepository) throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/", "/style.css", "/error").permitAll()
				.anyRequest().authenticated())
			.oauth2Login(Customizer.withDefaults())
			.logout((logout) -> logout
				.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)));
		return http.build();
	}

	// After the local logout, send the browser to OneLogin's end_session_endpoint so the
	// OneLogin session ends too. OneLogin then redirects back to this app's home page.
	private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(
			ClientRegistrationRepository clientRegistrationRepository) {
		OidcClientInitiatedLogoutSuccessHandler handler =
				new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
		handler.setPostLogoutRedirectUri("{baseUrl}/");
		return handler;
	}

}
