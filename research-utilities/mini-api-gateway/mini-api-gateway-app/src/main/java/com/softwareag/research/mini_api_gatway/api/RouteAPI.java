/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
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
import com.softwareag.research.mini_api_gatway.events.EventService;
import com.softwareag.research.mini_api_gatway.model.RouteEntity;
import com.softwareag.research.mini_api_gatway.model.RoutePath;
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

	@Autowired
	private EventService eventService;

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
		OpenAPI openApi = null;
		if (request.getSpecification() != null && !request.getSpecification().isBlank()) {
			openApi = parseSpecification(request);
		}
		if (request.getEndpoint() == null || request.getEndpoint().isBlank()) {
			if (openApi != null) {
				request.setEndpoint(getEndpoint(openApi));
			} else {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"No endpoint for the supplied. Either provide an HTTP(S) URL or a valid OpenAPI specification with a \"servers\" entry.");
			}
		}
		RouteEntity newRoute;
		try {
			newRoute = new RouteEntity(request);
		} catch (RuntimeException e) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e));
		}
		if (openApi != null) {
			RoutePath[] paths = getPaths(openApi);
			applyActiveValuesToPaths(paths, request.getPaths());
			newRoute.setPaths(paths);
		}
		return routeRepository.existsByNameAndVersion(request.getName(), request.getVersion()).flatMap(exists -> {
			if (exists) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"This combination of Route name and version already exists.");
			} else {
				return routeRepository.save(newRoute).doOnNext(rE -> eventService.publishRouteRefresh());
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
		OpenAPI openApi;
		if (request.getSpecification() != null && !request.getSpecification().isBlank()) {
			openApi = parseSpecification(request);
		} else {
			openApi = null;
		}
		if (request.getEndpoint() == null || request.getEndpoint().isBlank()) {
			if (openApi != null) {
				request.setEndpoint(getEndpoint(openApi));
			} else {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"No endpoint for the supplied. Either provide an HTTP(S) URL or a valid OpenAPI specification with a \"servers\" entry.");
			}
		}
		return routeRepository.findById(id)
				.switchIfEmpty(Mono.error(
						new ResponseStatusException(HttpStatus.NOT_FOUND, "Route with id %d not found".formatted(id))))
				.flatMap(routeEntity -> {
					try {
						if (openApi != null) {
							if (!routeEntity.getSpecification().equals(request.getSpecification())) {
								RoutePath[] newPaths = getPaths(openApi);
								// Migrate active settings to new paths
								applyActiveValuesToPaths(newPaths, routeEntity.getPaths());
								routeEntity.setPaths(newPaths);
							}
							// Apply active settings from request
							applyActiveValuesToPaths(routeEntity.getPaths(), request.getPaths());
						}
						routeEntity.setActive(request.isActive());
						routeEntity.setEndpoint(request.getEndpoint());
						routeEntity.setFilters(request.getFilters());
						routeEntity.setSpecification(request.getSpecification());
						routeEntity.setGlobalFilters(request.getGlobalFilters());
						routeEntity.setSettings(request.getSettings());
					} catch (RuntimeException e) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e));
					}
					return routeRepository.save(routeEntity);
				}).doOnNext(rE -> eventService.publishRouteRefresh()).map(mapRouteEntityToResponse);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(description = "Removes a route from the gateway")
	public Mono<Void> deleteRoute(@PathVariable long id) {
		return routeRepository.deleteById(id);
	}

	private OpenAPI parseSpecification(RouteRequest request) throws ResponseStatusException {
		if (request.getSpecification() == null || request.getSpecification().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Please provide a valid OpenAPI specification.");
		} else {
			OpenAPI specification = APISpecificationUtils.parseAPISpecification(request.getSpecification());
			if(specification == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"The OpenAPI specification contains errors. Please provide a valid OpenAPI specification.");
			} else {
				return specification;
			}
		}
	}

	private String getEndpoint(OpenAPI openAPI) throws ResponseStatusException {
		try {
			return openAPI.getServers().get(0).getUrl();
		} catch (IndexOutOfBoundsException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"No endpoint for the supplied. Either provide an HTTP(S) URL or a valid OpenAPI specification with a \"servers\" entry.");
		}
	}

	private RoutePath[] getPaths(OpenAPI openApi) {
		List<RoutePath> paths = new ArrayList<>();
		openApi.getPaths().forEach((path, pathItem) -> {
			pathItem.readOperationsMap().keySet().forEach(method -> {
				paths.add(new RoutePath(path, method, true));
			});
		});
		return paths.toArray(RoutePath[]::new);
	}

	private void applyActiveValuesToPaths(RoutePath[] baseValue, @Nullable RoutePath[] overrideValues) {
		if (overrideValues != null) {
			for (RoutePath newPath : baseValue) {
				for (RoutePath oldPath : overrideValues) {
					if (oldPath.equals(newPath)) {
						newPath.setActive(oldPath.isActive());
						break;
					}
				}
			}
		}
	}

}
