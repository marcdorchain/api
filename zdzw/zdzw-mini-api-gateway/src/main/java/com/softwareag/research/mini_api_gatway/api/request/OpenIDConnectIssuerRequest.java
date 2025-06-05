/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.api.request;

import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer.IssuerMatchMode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NonNull;

@Data
public class OpenIDConnectIssuerRequest {

	/** Issuer URL **/
	@Schema(description = "Issuer URL", example = "https://my-keycloak.com/auth/realms/my_realm")
	@NonNull
	private String issuer;

	/**
	 * Optional explicit Token Introspection URL if not in standard OpenID Connect
	 * scheme
	 **/
	@Schema(description = "Optional explicit Token Introspection URL if not in standard OpenID Connect scheme")
	private String tokenIntrospectionEndpoint;

	/** How the gateway should match a given issuer URL to this issuer **/
	@NonNull
	private IssuerMatchMode matchMode;

	/** Optional Token Introspection Client ID **/
	@Schema(description = "Optional Client ID for Token Introspection. Default from component setup will be used if ommitted")
	private String clientId;

	/** Optional Token Introspection Client Secret **/
	@Schema(description = "Optional Client Secret for Token Introspection. Default from component setup will be used if ommitted")
	private String clientSecret;

}
