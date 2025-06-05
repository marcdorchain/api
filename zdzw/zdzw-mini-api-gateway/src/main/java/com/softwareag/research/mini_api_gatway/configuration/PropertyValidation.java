/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;

@Component
public class PropertyValidation {

	@Autowired
	private Environment env;

	private void checkConfiguration() throws Exception {
		UriComponentsBuilder.fromHttpUrl(env.getProperty("gateway.public-url"));
		UriComponentsBuilder.fromHttpUrl(env.getProperty("security.token-validation.url"));
		if (env.getProperty("security.token-validation.mode") == "INTROSPECTION") {
			if (env.getProperty("security.token-validation.introspection.clientId") == null
					|| env.getProperty("security.token-validation.introspection.clientSecret") == null) {
				throw new IllegalArgumentException("A client ID and secret is needed to perform token introspection");
			}
		}
	}

	@PostConstruct
	public void init() {
		try {
			checkConfiguration();
		} catch (Exception e) {
			throw new RuntimeException("Invalid configuration supplied", e);
		}
	}

}
