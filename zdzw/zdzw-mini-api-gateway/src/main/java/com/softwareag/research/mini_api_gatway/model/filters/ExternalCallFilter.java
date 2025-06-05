/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.model.filters;

import java.util.Map;
import java.util.Map.Entry;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.softwareag.research.mini_api_gatway.utils.VariableExpressionResolver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * The ExternalCallFilter is a {@link RouteFilter} that allows calling an
 * HTTP(S) service <b>before</b> the main request is made. Information from the
 * response's headers or payload can be mapped to headers or query parameters of
 * the main request as well as custom variables.
 *
 * @author jonsch
 *
 */
@Schema(description = "Allows calling an"
		+ " HTTP(S) service <b>before</b> the main request is made. Information from the"
		+ " response's headers or payload can be mapped to headers or query parameters of"
		+ " the main request as well as custom variables.")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalCallFilter implements RouteFilter {

	private final RouteFilterType type = RouteFilterType.EXTERNAL_CALL;
	private final RouteFilterStage stage = RouteFilterStage.PRE_REQUEST;
	private static final String HEADERS_EXPR = "request.headers.";

	/** HTTP(S) URL to be requested **/
	@NonNull
	private String uri;
	/** HTTP Method for the request, e.g. GET, POST **/
	@NonNull
	private String method;
	/** Request payload **/
	@Nullable
	private String body;
	/** Request HTTP headers **/
	@JsonDeserialize(as = LinkedMultiValueMap.class)
	private MultiValueMap<String, String> headers;
	/**
	 * Mapping from response expression to variable expression. Key: variable
	 * expression (target), Value: response extraction expression (source)
	 **/
	private Map<String, String> responseMappings;

	@JsonIgnore
	private VariableExpressionResolver expressionResolver = new VariableExpressionResolver();
	@JsonIgnore
	private WebClient client = WebClient.create();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return client
			.method(HttpMethod.valueOf(method))
			.uri(uri, exchange.getAttributes())
			.headers((headers) -> headers.addAll(this.headers))
			.bodyValue(body)
			.exchangeToMono(response -> {
				if(response.statusCode().is2xxSuccessful()) {
						return response.bodyToMono(String.class).map(body -> {
							Builder request = exchange.getRequest().mutate();
							exchange.getAttributes().put("externalCallResponse", // Put response body and headers in
																					// variable externalCallResponse
									new CustomExtensionResponseContainer(body,
											response.headers().asHttpHeaders().toSingleValueMap()));

							for (Entry<String, String> responseMapping : responseMappings.entrySet()) {
								String value = expressionResolver.format(responseMapping.getValue(),
										exchange.getAttributes());
								String targetExpression = responseMapping.getKey();
								if (targetExpression.startsWith(HEADERS_EXPR)) {
									targetExpression = targetExpression.replace(HEADERS_EXPR, "");
									request.header(targetExpression, value);
								} else {
									exchange.getAttributes().put(targetExpression, value);
								}
							}
							return request.build();
						}).flatMap(request -> chain.filter(exchange.mutate().request(request).build())); //Forward mutated request to next filter in chain
				}else {
					return response.createError();
				}
			});
	}

	@SuppressWarnings("unused")
	private static class CustomExtensionResponseContainer {
		public String payload;
		public Map<String, String> headers;

		public CustomExtensionResponseContainer(String payload, Map<String, String> headers) {
			this.payload = payload;
			this.headers = headers;
		}
	}

}
