/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.model.filters;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link RouteFilter} that can be applied to multiple routes in parallel.
 */
@Data
@NoArgsConstructor
public class GlobalFilterEntity {

	/**
	 * Unique name for the global filter. Used as a reference from a Route to the
	 * Global Filter.
	 */
	@Id
	private String name;

	/**
	 * The {@link RouteFilter} definition.
	 */
	private RouteFilter filter;

	/** Version to support correct database handling */
	@Version
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	private int _version;
}
