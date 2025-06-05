/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.api;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.softwareag.research.mini_api_gatway.events.EventService;
import com.softwareag.research.mini_api_gatway.model.RouteRepository;
import com.softwareag.research.mini_api_gatway.model.filters.GlobalFilterEntity;
import com.softwareag.research.mini_api_gatway.model.filters.GlobalFilterRepository;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/global-filters")
@Tag(name = "Global Filters", description = "Global filters can be registered on gateway level and be applied to multiple routes simultaneously. Changes propagate to applied routes automatically.")
@Slf4j
public class GlobalFilterAPI {

	@Autowired
	private RouteRepository routeRepository;

	@Autowired
	private EventService eventService;

	@Autowired
	private GlobalFilterRepository gFilterRepository;

	@GetMapping()
	@Operation(description = "Lists all global filters in the gateway")
	public Flux<GlobalFilterEntity> getAllGlobalFilters() {
		return gFilterRepository.findAll();
	}

	@GetMapping("/{name}")
	@Operation(description = "Returns the definition of a specific global filter")
	public Mono<GlobalFilterEntity> getGlobalFilter(@PathVariable String name) {
		return gFilterRepository.findByName(name)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Global Filter with name %s not found".formatted(name))));
	}

	@PostMapping()
	@Operation(description = "Creates a new global filter in the gateway")
	public Mono<GlobalFilterEntity> createGlobalFilter(@RequestBody GlobalFilterEntity filter) {
		log.info("Request: {}", filter);
		return gFilterRepository.existsByName(filter.getName()).flatMap(exists -> {
			if (exists) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"A global filter with the name %s exists already.".formatted(filter.getName()));
			} else {
				return gFilterRepository.save(filter);
			}
		});
	}

	@PutMapping("/{name}")
	@Operation(description = "Updates an existing global filter in the gateway")
	public Mono<GlobalFilterEntity> updateGlobalFilter(@PathVariable String name,
			@RequestBody RouteFilter filter) {
		return gFilterRepository.findByName(name)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Global Filter with name %s not found".formatted(name))))
				.flatMap(filterEntity -> {
					filterEntity.setFilter(filter);
					return gFilterRepository.save(filterEntity);
				}).doOnNext(fE -> eventService.publishRouteRefresh());
	}

	@DeleteMapping("/{name}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(description = "Removes a global filter from the gateway")
	public Mono<Void> deleteGlobalFilter(@PathVariable String name) {
		return routeRepository.findAll().any(rE -> Arrays.stream(rE.getGlobalFilters()).anyMatch(f -> f.equals(name)))
				.flatMap(isUsed -> {
					if (isUsed) {
						throw new ResponseStatusException(HttpStatus.CONFLICT,
								"Global Filter is applied to a route and cannot be deleted.");
					} else {
						return gFilterRepository.deleteById(name);
					}
				});
	}

}
