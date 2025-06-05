/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.model.filters.zdzw;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilter;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilterStage;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilterType;
import com.softwareag.research.mini_api_gatway.utils.PropertiesValueUtil;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import reactor.core.publisher.Mono;

/**
 * {@link RouteFilter} that verifies, that the calling user has a valid license
 * for the called zApp. Verification is done by requesting licenses from ZDZW
 * License Manager using access token from request.
 */
@Schema(description = "Verify license for zApp using ZDZW License Manager")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZDZWLicenseVerificationFilter implements RouteFilter{

	private final RouteFilterType type = RouteFilterType.ZDZW_LICENSE_VERIFICATION;
	private final RouteFilterStage stage = RouteFilterStage.INCOMING;

	/** ZDZW Marketplace App ID **/
	@NonNull
	private String appId;

	@JsonIgnore
	private WebClient client = WebClient.create();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

		if (authHeader == null) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token was removed from request.");
		}

		String licenseManagerUrl = PropertiesValueUtil.getPropertyValue("zdzw.license-manager.url");

		return client.get().uri(licenseManagerUrl + "/api/v1/licenses/licensesByRealm")
				.accept(MediaType.APPLICATION_JSON).header("Authorization", authHeader).exchangeToMono(response -> {
					if (response.statusCode().is2xxSuccessful()) {
						return response.bodyToMono(ZDZWLicenseResponse[].class).flatMap(body -> {
							for (ZDZWLicenseResponse license : body) {
								if (license.getProductId().equals(appId) && license.status.equals("active")) {
									return chain.filter(exchange);
								}
							}
							return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
									"No valid license for the requested application found."));
						});
					} else {
						return response.createError();
					}
				});

	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class ZDZWLicenseResponse {

		@JsonProperty("product_id")
		private String productId;

		private String status;
	}

}
