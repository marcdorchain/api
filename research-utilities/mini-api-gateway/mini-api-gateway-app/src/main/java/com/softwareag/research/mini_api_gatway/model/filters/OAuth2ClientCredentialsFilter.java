/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.model.filters;


import java.util.Collection;
import java.util.Collections;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

//TODO: Finish and test
@Schema(description = "Authorize outgoing route request with an OAuth 2 token of grant type \"client_credentials\"."
		+ " The token will be acquired prior to the outgoing request using the supplied credentials and set as the"
		+ " Authorization header of the outgoing request using Bearer scheme.")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2ClientCredentialsFilter implements RouteFilter {

	private final RouteFilterType type = RouteFilterType.OAUTH2_CLIENT;
	private final RouteFilterStage stage = RouteFilterStage.PRE_REQUEST;

	private String clientId;

	private String clientSecret;

	private String issuerUrl;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private OAuth2AuthorizationContext context;
	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private OAuth2AuthorizedClient client;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private final ReactiveOAuth2AuthorizedClientProvider clientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder
			.builder().clientCredentials().build();

	private void initialize() {
		try {
			ClientRegistration clientRegistration = ClientRegistrations.fromIssuerLocation(issuerUrl).clientId(clientId)
					.clientSecret(clientSecret).authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
					.build();
			context = OAuth2AuthorizationContext.withClientRegistration(clientRegistration)
					.principal(new NoopAuthentication()).build();
		} catch (RuntimeException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Authorization with downstream server failed.");
		}
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (context == null) {
			initialize();
		}
		return clientProvider.authorize(context).switchIfEmpty(Mono.justOrEmpty(client)).flatMap(authorizedClient -> {
			this.client = authorizedClient;
			return chain.filter(exchange.mutate().request(request -> {
				request.header("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());
			}).build());
		});
	}

	private static class NoopAuthentication implements Authentication {

		private static final long serialVersionUID = 9035345460428428266L;

		@Override
		public String getName() {
			return "Noop";
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return Collections.emptyList();
		}

		@Override
		public Object getCredentials() {
			return null;
		}

		@Override
		public Object getDetails() {
			return null;
		}

		@Override
		public Object getPrincipal() {
			return null;
		}

		@Override
		public boolean isAuthenticated() {
			return false;
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			// Noop
		}

	}
}
