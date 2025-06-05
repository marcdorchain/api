/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.model.filters;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface GlobalFilterRepository extends ReactiveCrudRepository<GlobalFilterEntity, String> {

	Mono<GlobalFilterEntity> findByName(String name);

	Mono<Boolean> existsByName(String name);

}
