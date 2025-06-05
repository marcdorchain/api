/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.configuration;

public class Constants {
	public final static String GATEWAY_PROXY_PATH = "/gateway";
	public final static int GATEWAY_PROXY_PATH_PREFIX_LENGTH = 3;
	public final static String GATEWAY_PROXY_ENDPOINT_FORMAT = GATEWAY_PROXY_PATH+"/%s/%s";
	public final static String GATEWAY_PROXY_PATH_FORMAT = GATEWAY_PROXY_ENDPOINT_FORMAT+"/**";
}
