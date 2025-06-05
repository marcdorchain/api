/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.model.filters;

import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * {@link RouteFilter} to add one or more headers to route requests. Existing
 * header values will be overwritten.
 */
@Schema(description = "Add one or more headers to route requests. Existing header values will be overwritten.")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SetHeadersFilter implements RouteFilter {

	private final RouteFilterType type = RouteFilterType.SET_HEADERS;
	private final RouteFilterStage stage = RouteFilterStage.PRE_REQUEST;

	private Map<String, String> headers;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest().mutate()
				.headers(httpHeaders -> httpHeaders.setAll(headers)).build();

		return chain.filter(exchange.mutate().request(request).build());
	}

}
