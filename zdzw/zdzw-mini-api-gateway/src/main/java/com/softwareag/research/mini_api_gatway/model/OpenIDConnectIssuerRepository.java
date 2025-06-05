/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.model;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface OpenIDConnectIssuerRepository extends ReactiveCrudRepository<OpenIDConnectIssuer, Long> {

	Mono<OpenIDConnectIssuer> findByIssuer(String issuer);

}
