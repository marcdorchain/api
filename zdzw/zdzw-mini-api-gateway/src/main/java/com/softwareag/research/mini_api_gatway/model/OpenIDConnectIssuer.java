/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.model;

import javax.annotation.Nullable;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.softwareag.research.mini_api_gatway.api.request.OpenIDConnectIssuerRequest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Data object containing information about an authorization server that
 * supports the OpenID Connect protocol for issuing and verifying JWTs that
 * should be used for authentication at the gateway.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenIDConnectIssuer {

	@Id
	private Long id;

	/** Issuer URL **/
	@NonNull
	private String issuer;

	/**
	 * Optional explicit Token Introspection URL if not in standard OpenID Connect
	 * scheme
	 **/
	private String tokenIntrospectionEndpoint;

	/** How the gateway should match a given issuer URL to this issuer **/
	@NonNull
	private IssuerMatchMode matchMode;

	/** Optional Token Introspection Client ID **/
	@Nullable
	private String clientId;

	/** Optional Token Introspection Client Secret **/
	@Nullable
	private String clientSecret;

	public OpenIDConnectIssuer(OpenIDConnectIssuerRequest request) {
		this(null, request.getIssuer(), request.getTokenIntrospectionEndpoint(), request.getMatchMode(),
				request.getClientId(), request.getClientSecret());
	}

	/**
	 * Matching mode for issuer URLs. PREFIX_MATCH matches URLs for which
	 * String.startsWith returns true. FULL_MATCH matches URLs for which
	 * String.equals returns true.
	 *
	 * @author jonsch
	 *
	 */
	@JsonFormat(shape = Shape.STRING)
	public enum IssuerMatchMode {
		PREFIX_MATCH, FULL_MATCH
	}

}
