/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.softwareag.research.mini_api_gatway.utils.JwtUtils;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Log4j2
@NoArgsConstructor
@Component
public class MappingJwtGrantedAuthoritiesConverter implements Converter<Jwt, Flux<GrantedAuthority>> {

	@Value("${security.token-validation.rolesPath:scope}")
	private String rolePath;

	@Value("${security.token-validation.userRole}")
	private String userRole;

	@Value("${security.token-validation.adminRole}")
	private String adminRole;

    @Override
	public Flux<GrantedAuthority> convert(Jwt jwt) {

		Collection<String> roles = JwtUtils.parseRoles(jwt, rolePath);

		log.info("{}", roles);

		return Flux.fromIterable(JwtUtils.createAuthoritiesFromRoles(roles, userRole, adminRole));
    }


}