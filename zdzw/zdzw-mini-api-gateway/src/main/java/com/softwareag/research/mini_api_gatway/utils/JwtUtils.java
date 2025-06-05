/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtUtils {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	public static String getTokenIssuer(String token) throws JwtException {
		String payload = token.split("[.]")[1];
		JsonNode tokenJson;
		try {
			tokenJson = objectMapper.readTree(Base64.getDecoder().decode(payload));
		} catch (IOException e) {
			throw new JwtException(e.getMessage(), e);
		}
		return tokenJson.get("iss").asText();
	}

	public static Jwt parseJwt(String token) {
		return Jwt.withTokenValue(token).build();
	}

	public static Collection<String> parseRoles(Jwt jwt, String rolePath) {
		String[] pathItems = rolePath.split("\\.");
		int index = 0;
		Map<String, Object> currentMap = null;
		while (index < pathItems.length - 1) {
			if (currentMap == null) {
				try {
					currentMap = jwt.getClaimAsMap(pathItems[index]);
				} catch (IllegalArgumentException e) {
					return Collections.emptyList();
				}
			} else {
				currentMap = (Map<String, Object>) currentMap.getOrDefault(pathItems[index], null);
			}
			index++;
		}
		if (currentMap == null) {
			try {
				return jwt.getClaimAsStringList(pathItems[index]);
			} catch (IllegalArgumentException e) {
				return new ArrayList<>(Arrays.asList((jwt.getClaimAsString(pathItems[index]).split(" "))));
			} catch (NullPointerException e) {
				return Collections.emptyList();
			}
		} else {
			Object leafEntry = currentMap.getOrDefault(pathItems[index], null);
			if (leafEntry instanceof Collection) {
				return (Collection<String>) leafEntry;
			} else if (leafEntry instanceof String && leafEntry != null) {
				return new ArrayList<>(Arrays.asList(((String) leafEntry).split(" ")));
			} else {
				return Collections.emptyList();
			}
		}
	}

	public static Collection<String> parseRoles(Map<String, Object> map, String rolePath) {
		String[] pathItems = rolePath.split("\\.");
		int index = 0;
		Map<String, Object> currentMap = map;
		while (index < pathItems.length - 1) {
			if (currentMap == null)
				return Collections.emptyList();
			currentMap = (Map<String, Object>) currentMap.getOrDefault(pathItems[index], null);
			index++;
		}
		if (currentMap == null)
			return Collections.emptyList();
		Object leafEntry = currentMap.getOrDefault(pathItems[index], null);
		if (leafEntry instanceof Collection) {
			return (Collection<String>) leafEntry;
		} else if (leafEntry instanceof String && leafEntry != null) {
			return new ArrayList<>(Arrays.asList(((String) leafEntry).split(" ")));
		} else {
			return Collections.emptyList();
		}
	}

	public static Collection<GrantedAuthority> createAuthoritiesFromRoles(Collection<String> roles, String userRole,
			String adminRole) {

		HashSet<GrantedAuthority> set;
		if (roles == null) {
			set = new HashSet<>(1);
		} else {
			set = roles.stream().<SimpleGrantedAuthority>mapMulti((role, c) -> {
				if (role.equals(userRole)) {
					c.accept(new SimpleGrantedAuthority("ROLE_USER"));
				}
				if (role.equals(adminRole)) {
					c.accept(new SimpleGrantedAuthority("ROLE_ADMIN"));
				}
				c.accept(new SimpleGrantedAuthority(role));
			}).collect(Collectors.toCollection(HashSet::new));
		}

		set.add(new SimpleGrantedAuthority("ROLE_USER"));
		return set;
	}
}
