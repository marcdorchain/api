/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.model.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A RouteFilter is a custom class that allows transforming request and/or
 * response data before or after the request to the real API endpoint is made.
 *
 * @author jonsch
 *
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @Type(value = ExternalCallFilter.class, name = "EXTERNAL_CALL"),
	@Type(value = SetHeadersFilter.class, name = "SET_HEADERS"),
	@Type(value = OAuth2ClientCredentialsFilter.class, name = "OAUTH2_CLIENT"),
    @Type(value = RemoveRequestHeadersFilter.class, name = "REMOVE_REQUEST_HEADERS")
})
public interface RouteFilter extends GatewayFilter {

	public RouteFilterType getType();

	public RouteFilterStage getStage();
}
