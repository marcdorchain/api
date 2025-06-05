/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.stereotype.Component;

import com.softwareag.research.mini_api_gatway.utils.JwtUtils;

import reactor.core.publisher.Mono;

/**
 * Decoder for JWT tokens with validation by issuer, signature and expiry
 * timestamp
 *
 * @author jonsch
 *
 */
@Component
public class JwtDecoder implements ReactiveJwtDecoder {

	private final ConcurrentHashMap<String, NimbusReactiveJwtDecoder> decoderCache = new ConcurrentHashMap<>();

	private final OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(Duration.ofSeconds(60)));

	@Autowired
	private IssuerValidationService issuerValidationService;

	@Override
	public Mono<Jwt> decode(String token) throws JwtException {
		String issuer = JwtUtils.getTokenIssuer(token);
		issuerValidationService.validateOidcIssuer(issuer);

		NimbusReactiveJwtDecoder decoder = null;
		if(decoderCache.containsKey(issuer)) {
			decoder = decoderCache.get(issuer);
		}else {
			decoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders.fromOidcIssuerLocation(issuer);
			decoder.setJwtValidator(withClockSkew);
			decoderCache.put(issuer, decoder);
		}
	    return decoder.decode(token);
	}

}
