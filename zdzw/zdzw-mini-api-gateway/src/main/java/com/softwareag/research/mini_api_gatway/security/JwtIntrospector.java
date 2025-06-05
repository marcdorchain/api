/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.stereotype.Component;

import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer;
import com.softwareag.research.mini_api_gatway.utils.JwtUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Decoder for JWT tokens with validation by issuer and token introspection
 *
 * @author jonsch
 *
 */
@Slf4j
@Component
public class JwtIntrospector implements ReactiveOpaqueTokenIntrospector {

	private final static String KEYLOAK_INTROSPECTION_ENDPOINT = "/protocol/openid-connect/token/introspect";

	private final ConcurrentHashMap<String, NimbusReactiveOpaqueTokenIntrospector> introspectorCache = new ConcurrentHashMap<>();

	@Value("${security.token-validation.introspection.clientid:#{null}}")
	private String defaultClientId;

	@Value("${security.token-validation.introspection.clientsecret:#{null}}")
	private String defaultClientSecret;

	@Value("${security.token-validation.mode}")
	private String tokenValidationMode;

	@Value("${security.token-validation.rolePath:scope}")
	private String rolePath;

	@Value("${security.token-validation.userRole}")
	private String userRole;

	@Value("${security.token-validation.adminRole}")
	private String adminRole;

	@PostConstruct
	private void validateConfiguration() {
		if (tokenValidationMode.equals("INTROSPECTION")) {
			if (defaultClientId == null || defaultClientId.isBlank() || defaultClientSecret == null
					|| defaultClientSecret.isBlank()) {
				log.error("Default Client ID and Secret missing");
				throw new IllegalArgumentException(
						"Default Client ID and Secret must be set when using token introspection");
			}
		}
	}

	@Autowired
	private IssuerValidationService issuerValidationService;

	@Override
	public Mono<OAuth2AuthenticatedPrincipal> introspect(String token) throws JwtException {
		String issuer = JwtUtils.getTokenIssuer(token);
		OpenIDConnectIssuer oidcIssuer = issuerValidationService.validateOidcIssuer(issuer);

		String introspectionUrl = oidcIssuer.getTokenIntrospectionEndpoint();
		if (introspectionUrl == null) {
			introspectionUrl = issuer + KEYLOAK_INTROSPECTION_ENDPOINT;
		}

		NimbusReactiveOpaqueTokenIntrospector introspector = null;
		if (introspectorCache.containsKey(introspectionUrl)) {
			introspector = introspectorCache.get(introspectionUrl);
		}else {
			String clientId = this.defaultClientId;
			if (oidcIssuer.getClientId() != null) {
				clientId = oidcIssuer.getClientId();
			}
			String clientSecret = this.defaultClientSecret;
			if (oidcIssuer.getClientSecret() != null) {
				clientSecret = oidcIssuer.getClientSecret();
			}
			introspector = new NimbusReactiveOpaqueTokenIntrospector(introspectionUrl, clientId, clientSecret);
			introspectorCache.put(introspectionUrl, introspector);
		}
		return introspector.introspect(token).map(p -> {
			return new OAuth2IntrospectionAuthenticatedPrincipal(p.getAttributes(),
					JwtUtils.createAuthoritiesFromRoles(JwtUtils.parseRoles(p.getAttributes(), rolePath), userRole,
							adminRole));
		});
	}

}
