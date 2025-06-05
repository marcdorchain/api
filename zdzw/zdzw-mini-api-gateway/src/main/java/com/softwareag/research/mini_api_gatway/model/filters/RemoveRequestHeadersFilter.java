/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.model.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Schema(description = "Remove HTTP headers from incoming requests, resulting in those headers"
		+ " not being forwarded to the target API.")
@Data
@NoArgsConstructor
public class RemoveRequestHeadersFilter implements RouteFilter {

	private final RouteFilterType type = RouteFilterType.REMOVE_REQUEST_HEADERS;
	private final RouteFilterStage stage = RouteFilterStage.INCOMING;

	private String[] headersToRemove;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest().mutate().headers(httpHeaders -> {
			for (String header : headersToRemove) {
				httpHeaders.remove(header);
			}
		}).build();

		return chain.filter(exchange.mutate().request(request).build());
	}

}
