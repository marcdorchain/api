/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.model;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface RouteRepository extends ReactiveCrudRepository<RouteEntity, Long>{

	Mono<RouteEntity> findFirstByNameAndVersion(String name, String version);

	Mono<RouteEntity> findById(long id);

	Mono<Boolean> existsByNameAndVersion(String name, String version);

}
