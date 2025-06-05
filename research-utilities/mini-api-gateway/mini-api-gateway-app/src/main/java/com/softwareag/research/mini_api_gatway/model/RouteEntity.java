/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.model;

import org.springframework.data.annotation.Id;
import org.springframework.web.util.UriComponentsBuilder;

import com.softwareag.research.mini_api_gatway.api.request.RouteRequest;
import com.softwareag.research.mini_api_gatway.model.filters.GlobalFilterEntity;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Database object containing information about an API (route) in the gateway
 *
 * @author jonsch
 *
 */
@Data
@NoArgsConstructor
@ToString(exclude = {"specification"})
public class RouteEntity {

	@Id
	@Schema(description = "Unique identifier for the Route/API")
	private Long id;

	/** Route/API name (part of the gateway endpoint) **/
	@NonNull
	@Schema(description = "Route/API name (part of the gateway endpoint)")
	private String name;

	/** Route/API version (part of the gateway endpoint) **/
	@NonNull
	@Schema(description = "Route/API version (part of the gateway endpoint)")
	private String version;

	/** Optional OpenAPI specification **/
	@Schema(description = "Optional OpenAPI specification")
	private String specification;

	/** HTTP(S) URL to the API **/
	@NonNull
	@Schema(description = "HTTP(S) URL to the API")
	private String endpoint;

	/**
	 * Paths and Operations defined in specification. Will be generated on creation.
	 * Only \"active\" values should be updated. Is null of no specification was
	 * provided.
	 **/
	@Schema(description = "Paths and Operations defined in specification. Will be generated on creation. Only \"active\" values should be updated.")
	private RoutePath[] paths;

	/** Detailed/advanced settings for gateway functions **/
	@NonNull
	@Schema(description = "Detailed/advanced settings for gateway functions")
	private RouteSettings settings;

	public void setEndpoint(String endpoint) {
		UriComponentsBuilder.fromHttpUrl(endpoint); // Validate URL
		this.endpoint = endpoint;
	}

	/** Whether the route is activated and accessable **/
	@Schema(description = "Whether the route is activated and accessible. Default is true")
	private boolean active;

	/**
	 * Custom {@link RouteFilter}s transforming request/response data of this route
	 **/
	@Schema(description = "Custom filters to transform request/response data of this route")
	private RouteFilter[] filters;

	/**
	 * Names of {@link GlobalFilterEntity}s to apply to this route
	 */
	@Schema(description = "Names of Global Filters to apply to this route")
	private String[] globalFilters;


	public RouteEntity(RouteRequest request) {
		setName(request.getName());
		setVersion(request.getVersion());
		setSpecification(request.getSpecification());
		setEndpoint(request.getEndpoint());
		setActive(request.isActive());
		setFilters(request.getFilters());
		setGlobalFilters(request.getGlobalFilters());
		setSettings(request.getSettings());
	}
}
