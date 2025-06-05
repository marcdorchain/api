/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.api.request;

import com.softwareag.research.mini_api_gatway.model.filters.GlobalFilterEntity;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@ToString(exclude = { "specification" })
public class RouteRequest {

	@NonNull
	@NotNull
	@Schema(description = "Route/API name (part of the gateway endpoint)")
	/** Route/API name (part of the gateway endpoint) **/
	private String name;

	@NonNull
	@NotNull
	@Schema(description = "Route/API version (part of the gateway endpoint)")
	/** Route/API version (part of the gateway endpoint) **/
	private String version;

	@Schema(description = "Optional OpenAPI specification")
	/** Optional OpenAPI specification **/
	private String specification;

	@Schema(description = "HTTP(S) URL to the API.  If not supplied, a valid OpenAPI specification with a servers entry must be supplied")
	/**
	 * HTTP(S) URL to the API. If not supplied, a valid OpenAPI specification with a
	 * servers entry must be supplied
	 **/
	private String endpoint;

	@Schema(description = "Whether the route is activated and accessible. Default is true")
	/** Whether the route is activated and accessible. Default is true **/
	private boolean active = true;

	/**
	 * Custom {@link RouteFilter}s transforming request/response data of this route
	 **/
	@Schema(description = "Custom filters to transform request/response data of this route")
	private RouteFilter[] filters;

	/**
	 * Names of {@link GlobalFilterEntity}s to apply to this route
	 **/
	@Schema(description = "Names of Global Filters to apply to this route")
	private String[] globalFilters;
}