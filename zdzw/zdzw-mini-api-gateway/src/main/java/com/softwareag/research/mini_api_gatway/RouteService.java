/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.softwareag.research.mini_api_gatway.configuration.Constants;
import com.softwareag.research.mini_api_gatway.model.RouteEntity;
import com.softwareag.research.mini_api_gatway.model.RouteRepository;
import com.softwareag.research.mini_api_gatway.model.filters.GlobalFilterEntity;
import com.softwareag.research.mini_api_gatway.model.filters.GlobalFilterRepository;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Reads {@link RouteEntity RouteEntities} from database to create gateway
 * endpoints
 *
 * @author jonsch
 *
 */
@Service
@Slf4j
public class RouteService implements RouteLocator {

	@Autowired
	private RouteRepository routeRepository;

	@Autowired
	private GlobalFilterRepository gFilterRepository;

	@Autowired
	private RouteLocatorBuilder builder;

	@Value("${gateway.public-url}")
	private String publicGatewayUrl;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public Flux<Route> getRoutes() {
		Builder routeBuilder = builder.routes();
		return gFilterRepository.findAll().collectMap(fE -> fE.getName())
				.flatMapMany(filterMap -> buildRoutes(routeBuilder, filterMap));
	}

	private Flux<Route> buildRoutes(Builder routeBuilder, Map<String, GlobalFilterEntity> globalFilters) {
		return routeRepository.findAll()
				.filter(rE -> rE.isActive())
				.reduce(routeBuilder, (rB, routeEntity) -> {
					try {
					log.info("Loading route: {}",routeEntity);
					UriComponents uri = UriComponentsBuilder.fromHttpUrl(routeEntity.getEndpoint()).build();
					return rB.route(routeEntity.getId().toString(), r ->
			    		r.
						path(Constants.GATEWAY_PROXY_PATH_FORMAT.formatted(routeEntity.getName(),
								routeEntity.getVersion()))
			    			.filters(f -> {
			    				GatewayFilterSpec filters = f.stripPrefix(Constants.GATEWAY_PROXY_PATH_PREFIX_LENGTH);
			    				if(uri.getPath() != null && !uri.getPath().isEmpty()) {
			    					filters = filters.prefixPath(uri.getPath());
			    				}
			    				if(uri.getQuery() != null) {
			    					for(Entry<String, List<String>> entry : uri.getQueryParams().entrySet()) {
			    						for(String value : entry.getValue()) {
			    							filters = filters.addRequestParameter(entry.getKey(), value);
			    						}
			    					}
			    				}
			    				List<RouteFilter> specificFilters = new ArrayList<>(
			    						(routeEntity.getGlobalFilters() == null ? 0 : routeEntity.getGlobalFilters().length) +
			    						(routeEntity.getFilters() == null ? 0 : routeEntity.getFilters().length));
								if (routeEntity.getGlobalFilters() != null) {
									specificFilters.addAll(Arrays.stream(routeEntity.getGlobalFilters())
											.map(name -> globalFilters.get(name))
											.map(gFilter -> gFilter.getFilter())
											.toList());
								}
			    				if(routeEntity.getFilters() != null) {
			    					for(RouteFilter filter : routeEntity.getFilters()) {
			    						specificFilters.add(filter);
			    					}
			    				}
			    				if(!specificFilters.isEmpty()) {
				    				specificFilters.sort(stageComparator);
				    				int i = 0;
				    				for(RouteFilter filter : specificFilters) {
				    					filters = filters.filter(filter, i++);
				    				}
			    				}
			    				return filters;
			    			})
						.uri(uri.toUriString())
			    	);
					} catch (RuntimeException e) {
						log.error("Could not load route {}", routeEntity, e);
						routeEntity.setActive(false);
						routeRepository.save(routeEntity).subscribe();
						return rB;
					}
				}).flatMapMany(builder -> {
					builder = generateUIRoutes(builder);
					return builder.build().getRoutes();
				});
	}

	public void refreshRoutes() {
		log.info("Refreshing route cache");
		applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
	}

	public String buildEndpoint(RouteEntity route) {
		return UriComponentsBuilder.fromHttpUrl(publicGatewayUrl).path(
				Constants.GATEWAY_PROXY_ENDPOINT_FORMAT.formatted(route.getName(),
								route.getVersion()))
				.toUriString();
	}

	private static Builder generateUIRoutes(Builder builder) {
		return builder.route("ui_redirect",
				r -> r.path("/", "/ui", "/ui/routes/**", "/ui/global-filters/**", "/ui/settings/**")
						.uri("forward:/ui/index.html"));
	}
	
	private final static Comparator<RouteFilter> stageComparator = (RouteFilter a, RouteFilter b) -> a.getStage().compareTo(b.getStage());

}
