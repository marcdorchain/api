/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.softwareag.research.mini_api_gatway.api.request.OpenIDConnectIssuerRequest;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuerRepository;
import com.softwareag.research.mini_api_gatway.security.RefreshIssuersEvent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Allows managing the trusted Open ID Connect issuers that can access the
 * gateway.
 *
 * @author jonsch
 *
 */
@RestController
@RequestMapping("/access")
@Slf4j
@Tag(name = "Gateway access management", description = "Allows managing the trusted Open ID Connect issuers that can access the gateway.")
public class AccessMgmtAPI {

	@Autowired
	private OpenIDConnectIssuerRepository oidcIssuerRepo;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Operation(description = "Lists the OpenID Connect Issuers registered in the gateway")
	@GetMapping("/oidc-issuers")
	public Flux<OpenIDConnectIssuer> getOidcIssuers() {
		return oidcIssuerRepo.findAll();
	}

	@Operation(description = "Registers a new OpenID Connect Issuer with the gateway", responses = {
			@ApiResponse(responseCode = "201", description = "The OpenID Connect issuer has been successfully added to the trusted list"),
			@ApiResponse(responseCode = "409", description = "The issuer url is already registered."),
			@ApiResponse(responseCode = "500", description = "An unknown error occured. Please check the response for details.") })
	@PostMapping("/oidc-issuers")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<OpenIDConnectIssuer> allowOidcIssuer(@RequestBody OpenIDConnectIssuerRequest request) {
		OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(request);
		return oidcIssuerRepo.save(issuer).doOnError(error -> {
			if (error instanceof DuplicateKeyException) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Issuer could not be added because it's url is already registered.");
			} else {
				log.error("Failed to save new OIDC Issuer to database", error);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"An unknown fatal error occured while adding the issuer. Please try again.");
			}
		}).doOnNext(e -> applicationEventPublisher.publishEvent(new RefreshIssuersEvent(this)));
	}

	@Operation(description = "Update an OpenID Connect Issuer that is already registered with the gateway", responses = {
			@ApiResponse(responseCode = "200", description = "The OpenID Connect issuer has been successfully updated"),
			@ApiResponse(responseCode = "400", description = "The request is malformed. Please check your request and try again."),
			@ApiResponse(responseCode = "409", description = "The issuer url is already registered."),
			@ApiResponse(responseCode = "500", description = "An unknown error occured. Please check the response for details.") })
	@PutMapping("/oidc-issuers/{id}")
	@ResponseStatus(code = HttpStatus.OK)
	public Mono<OpenIDConnectIssuer> updateOidcIssuer(@PathVariable long id,
			@RequestBody OpenIDConnectIssuerRequest request) {
		return oidcIssuerRepo.findById(id).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
				"The Issuer with id %d was not found.".formatted(id)))).flatMap(issuer -> {
					try {
						issuer.setClientId(request.getClientId());
						issuer.setClientSecret(request.getClientSecret());
						issuer.setIssuer(request.getIssuer());
						issuer.setMatchMode(request.getMatchMode());
						issuer.setTokenIntrospectionEndpoint(request.getTokenIntrospectionEndpoint());
					} catch (RuntimeException e) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e));
					}
					return oidcIssuerRepo.save(issuer);
				}).doOnError(error -> {
					if (error instanceof DuplicateKeyException) {
						throw new ResponseStatusException(HttpStatus.CONFLICT,
								"Issuer could not be added because it's url is already registered.");
					} else {
						log.error("Failed to save new OIDC Issuer to database", error);
						throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
								"An unknown fatal error occured while adding the issuer. Please try again.");
					}
				}).doOnNext(e -> applicationEventPublisher.publishEvent(new RefreshIssuersEvent(this)));
	}

	@Operation(description = "Get a specific OpenID Connect issuer by its ID", responses = {
			@ApiResponse(responseCode = "200", description = "The OpenID Connect issuer has been found."),
			@ApiResponse(responseCode = "404", description = "The requested OpenID Connect issuer could not be found."),
			@ApiResponse(responseCode = "500", description = "An unknown error occured. Please check the response for details.") })
	@GetMapping("/oidc-issuers/{id}")
	public Mono<OpenIDConnectIssuer> getOidcIssuer(@PathVariable long id) {
		return oidcIssuerRepo.findById(id).switchIfEmpty(
				Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						"The Issuer with id %d was not found.".formatted(id))));
	}

	@Operation(description = "Removes an OpenID Connect Issuer from the gateway", responses = {
			@ApiResponse(responseCode = "201", description = "If the OpenID Connect issuer was found it was successfully deleted."),
			@ApiResponse(responseCode = "500", description = "An unknown error occured. Please check the response for details.") })
	@DeleteMapping("/oidc-issuers/{id}")
	public Mono<Void> removeOidcIssuer(@PathVariable long id) {
		return oidcIssuerRepo.deleteById(id)
				.doOnNext(e -> applicationEventPublisher.publishEvent(new RefreshIssuersEvent(this)));
	}

}
