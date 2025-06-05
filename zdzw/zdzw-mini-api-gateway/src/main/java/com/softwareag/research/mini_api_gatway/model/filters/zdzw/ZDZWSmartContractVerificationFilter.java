/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.model.filters.zdzw;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilter;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilterStage;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilterType;
import com.softwareag.research.mini_api_gatway.utils.PropertiesValueUtil;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link RouteFilter} that verifies, that the calling user owns the requested smart contract.
 * Verification is done by requesting smart contracts from ZDZW License Manager using access token from request.
 */
@Schema(description = "Verify smart contract using ZDZW License Manager")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZDZWSmartContractVerificationFilter implements RouteFilter{

	private final RouteFilterType type = RouteFilterType.ZDZW_SMARTCONTRACT_VERIFICATION;
	private final RouteFilterStage stage = RouteFilterStage.INCOMING;

	@JsonIgnore
	private WebClient client = WebClient.create();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (!exchange.getRequest().getMethod().equals(HttpMethod.POST)) {
			return chain.filter(exchange);
		}


		return DataBufferUtils.join(exchange.getRequest().getBody(), 1024 * 10)
				.flatMap(buffer -> {
					InputStream is = buffer.asInputStream();
					BlockchainRequest requestBody;
					try {
						requestBody = new ObjectMapper().readValue(is, BlockchainRequest.class);
						is.close();
						buffer.readPosition(0);
					} catch (IOException e) {
						throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
					}

			if (requestBody.smartContractID == null) {
						// System.out.println("Contract ID is null");
				return chain.filter(
								exchange.mutate().request(requestFromCache(exchange, buffer)).build());
			} else {
				String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

				if (authHeader == null) {
							// System.out.println("Authheader is null");
					return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
							"Token was removed from request."));
				}

				String licenseManagerUrl = PropertiesValueUtil.getPropertyValue("zdzw.license-manager.url");

				return client.get().uri(licenseManagerUrl + "/api/v1/licenses/smartContractsByOrg")
						.accept(MediaType.APPLICATION_JSON).header("Authorization", authHeader)
						.exchangeToMono(response -> {
							if (response.statusCode().is2xxSuccessful()) {
								return response.bodyToMono(String[].class).flatMap(body -> {
									for (String smartContractId : body) {
										if (smartContractId.equals(requestBody.smartContractID)) {
													// System.out.println("SmartcontractID found");
											return chain.filter(exchange.mutate()
															.request(requestFromCache(exchange, buffer)).build());
										}
									}
											// System.out.println("No smartcontractid found");
									return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
											"Requested smart contract is not owned by authorized user."));
								});
							} else {
										// System.out.println("License manager failed");
								return response.createError();
							}
						});
			}
		});

	}

	ServerHttpRequestDecorator requestFromCache(ServerWebExchange exchange, DataBuffer buffer) {
		return new ServerHttpRequestDecorator(exchange.getRequest()) {
			@Override
			public Flux<DataBuffer> getBody() {
				return Flux.from(Mono.just(buffer));
			}
		};
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class BlockchainRequest {

		@JsonProperty("SmartContractID")
		@Nullable
		private String smartContractID;

	}

}
