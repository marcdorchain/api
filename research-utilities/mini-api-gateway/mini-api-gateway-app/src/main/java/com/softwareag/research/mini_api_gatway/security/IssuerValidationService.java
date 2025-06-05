/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.security;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.softwareag.research.mini_api_gatway.events.RefreshIssuersEvent;
import com.softwareag.research.mini_api_gatway.exceptions.IssuerValidationException;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer.IssuerMatchMode;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuerRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service that checks whether a supplied JWT token is allowed to access the
 * gateway routes. It checks whether the token issuer is registered in the
 * gateway as well as the token validity. Token validation strategies are by
 * signature and by token introspection.
 *
 * @author jonsch
 *
 */
@Service
@Slf4j
public class IssuerValidationService {

	@Autowired
	private OpenIDConnectIssuerRepository oidcIssuerRepo;

	private HashMap<String, HashMap<String, Set<OpenIDConnectIssuer>>> issuerCache;

	@Value("${security.token-validation.url:#{null}}")
	private String defaultIssuer;

	@Async
	@EventListener({ ApplicationPreparedEvent.class, RefreshIssuersEvent.class })
	public Mono<Void> refreshIssuerCache() {
		try {
		log.info("Refreshing Token Issuer Cache");
		issuerCache = new HashMap<String, HashMap<String, Set<OpenIDConnectIssuer>>>(2);
		issuerCache.put("http", new HashMap<String, Set<OpenIDConnectIssuer>>());
		issuerCache.put("https", new HashMap<String, Set<OpenIDConnectIssuer>>());
		return oidcIssuerRepo.findAll().doOnError(error -> log.error("Error loading issuers", error))
				.doOnNext(issuer -> {
			URI issuerURI = URI.create(issuer.getIssuer());
					HashMap<String, Set<OpenIDConnectIssuer>> schemeMap = issuerCache.computeIfAbsent(
							issuerURI.getScheme(), (a) -> new HashMap<String, Set<OpenIDConnectIssuer>>());
					Set<OpenIDConnectIssuer> set = schemeMap.computeIfAbsent(
							issuerURI.getHost() + ":" + issuerURI.getPort(),
							(a) -> new HashSet<OpenIDConnectIssuer>());
			set.add(issuer);
			log.info("Loaded issuer {}", issuer);
				}).then();
	} catch (Exception e) {
		log.error(e.getMessage(), e);
		return Mono.empty();
	}
	}

	/**
	 * Check whether the token issuer matches any registered OpenID Connect Issuer
	 *
	 * @param issuer
	 * @return the registered OIDC Issuer if matched
	 * @throws IssuerValidationException if no registered issuer matched
	 */
	public OpenIDConnectIssuer validateOidcIssuer(String issuer) throws IssuerValidationException {
		URI issuerURI = URI.create(issuer);
		HashMap<String, Set<OpenIDConnectIssuer>> schemeMap = issuerCache.get(issuerURI.getScheme());
		Set<OpenIDConnectIssuer> set = schemeMap.get(issuerURI.getHost() + ":" + issuerURI.getPort());
		if (set == null) {
			if (issuer.startsWith(defaultIssuer)) {
				return new OpenIDConnectIssuer(null, defaultIssuer, null, IssuerMatchMode.PREFIX_MATCH, null, null);
			}
			throw new IssuerValidationException("Token Issuer Host is not whitelisted.");
		}
		for (OpenIDConnectIssuer iss : set) {
			switch (iss.getMatchMode()) {
			case PREFIX_MATCH:
				if (issuer.startsWith(iss.getIssuer())) {
					return iss;
				}
				break;
			case FULL_MATCH:
				if (issuer.equals(iss.getIssuer())) {
					return iss;
				}
				break;
			default:
				log.warn("OpenIDConnectIssuer with unknown match mode");
			}
		}
		if (issuer.startsWith(defaultIssuer)) {
			return new OpenIDConnectIssuer(null, defaultIssuer, null, IssuerMatchMode.PREFIX_MATCH, null, null);
		}
		throw new IssuerValidationException("Token Issuer is not whitelisted");
	}

}
