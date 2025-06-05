/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.softwareag.research.mini_api_gatway.RouteService;
import com.softwareag.research.mini_api_gatway.api.request.RouteRequest;
import com.softwareag.research.mini_api_gatway.api.response.RouteResponse;
import com.softwareag.research.mini_api_gatway.model.RouteEntity;
import com.softwareag.research.mini_api_gatway.model.RouteRepository;
import com.softwareag.research.mini_api_gatway.utils.APISpecificationUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/routes")
@Tag(name = "Route (API) management")
@Slf4j
public class RouteAPI {

	@Autowired
	private RouteRepository routeRepository;

	@Autowired
	private RouteService routeService;

	private final Function<RouteEntity, RouteResponse> mapRouteEntityToResponse = rE -> new RouteResponse(rE,
			routeService.buildEndpoint(rE));

	@GetMapping
	@Operation(description = "Lists the routes registered in the gateway")
	public Flux<RouteResponse> getRoutes(
			@RequestParam @Parameter(description = "Comma-separated list of IDs of routes that should be included in the result", example = "1,5,231", style = ParameterStyle.SIMPLE) Optional<List<Long>> ids) {
		Flux<RouteEntity> query = routeRepository.findAll();
		if (ids.isPresent()) {
			query = query.filter(rE -> ids.get().contains(rE.getId()));
		}
		return query.map(mapRouteEntityToResponse);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(description = "Creates a new route in the gateway")
	public Mono<RouteResponse> createRoute(@RequestBody RouteRequest request) {
		if (request.getEndpoint() == null || request.getEndpoint().isBlank()) {
			request.setEndpoint(parseEndpointFromSpecification(request));
		}
		RouteEntity newRoute;
		try {
			newRoute = new RouteEntity(request);
		} catch (RuntimeException e) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e));
		}
		return routeRepository.existsByNameAndVersion(request.getName(), request.getVersion()).flatMap(exists -> {
			if (exists) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"This combination of Route name and version already exists.");
			} else {
				return routeRepository.save(newRoute).doOnNext(rE -> routeService.refreshRoutes());
			}
		}).map(mapRouteEntityToResponse);
	}

	@GetMapping("/{id}")
	@Operation(description = "Retrieve a specific route in the gateway")
	public Mono<RouteResponse> getRoute(@PathVariable long id) {
		return routeRepository.findById(id)
				.switchIfEmpty(Mono.error(
						new ResponseStatusException(HttpStatus.NOT_FOUND, "Route with id %d not found".formatted(id))))
				.map(mapRouteEntityToResponse);
	}

	@PutMapping("/{id}")
	@Operation(description = "Updates an existing route in the gateway")
	public Mono<RouteResponse> updateRoute(@PathVariable long id, @RequestBody RouteRequest request) {
		if (request.getEndpoint() == null || request.getEndpoint().isBlank()) {
			request.setEndpoint(parseEndpointFromSpecification(request));
		}
		return routeRepository.findById(id)
				.switchIfEmpty(Mono.error(
						new ResponseStatusException(HttpStatus.NOT_FOUND, "Route with id %d not found".formatted(id))))
				.flatMap(routeEntity -> {
					try {
						routeEntity.setActive(request.isActive());
						routeEntity.setEndpoint(request.getEndpoint());
						routeEntity.setFilters(request.getFilters());
						routeEntity.setSpecification(request.getSpecification());
						routeEntity.setGlobalFilters(request.getGlobalFilters());
					} catch (RuntimeException e) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e));
					}
					return routeRepository.save(routeEntity);
				}).doOnNext(rE -> routeService.refreshRoutes()).map(mapRouteEntityToResponse);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(description = "Removes a route from the gateway")
	public Mono<Void> deleteRoute(@PathVariable long id) {
		return routeRepository.deleteById(id);
	}

	private String parseEndpointFromSpecification(RouteRequest request) throws ResponseStatusException {
		if (request.getSpecification() == null || request.getSpecification().isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"No endpoint for the supplied. Either provide an HTTP(S) URL or a valid OpenAPI specification with a \"servers\" entry.");
		} else {
			OpenAPI specification = APISpecificationUtils.parseAPISpecification(request.getSpecification());
			try {
				return specification.getServers().get(0).getUrl();
			} catch (IndexOutOfBoundsException e) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"No endpoint for the supplied. Either provide an HTTP(S) URL or a valid OpenAPI specification with a \"servers\" entry.");
			}
		}
	}

}
