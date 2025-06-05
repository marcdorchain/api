/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.model.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

//TODO: Finish and test
@Schema(description = "Authorize outgoing route request with an OAuth 2 token of grant type \"client_credentials\"."
		+ " The token will be acquired prior to the outgoing request using the supplied credentials and set as the"
		+ " Authorization header of the outgoing request using Bearer scheme.")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2ClientCredentialsFilter implements RouteFilter {

	private final RouteFilterType type = RouteFilterType.OAUTH2_CLIENT;
	private final RouteFilterStage stage = RouteFilterStage.PRE_REQUEST;

	/** OAuth 2 Client ID **/
	private String clientId;
	/** OAuth 2 Client Secret **/
	private String clientSecret;
	/** Token Acquisition URL (Authorization Server) **/
	private String issuerUrl;

	@JsonIgnore
	@Getter(value = AccessLevel.NONE)
	private final ReactiveOAuth2AuthorizedClientProvider clientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder
			.builder().clientCredentials().build();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ClientRegistration clientRegistration = ClientRegistrations.fromIssuerLocation(issuerUrl)
				.clientId(clientId)
				.clientSecret(clientSecret)
				.build();
		OAuth2AuthorizationContext context = OAuth2AuthorizationContext.withClientRegistration(clientRegistration)
				.build();
		return clientProvider.authorize(context).flatMap(authorizedClient -> {
			Builder request = exchange.getRequest().mutate();
			request.header("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());
			return chain.filter(exchange.mutate().request(request.build()).build());
		});
	}
}
