/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.softwareag.research.mini_api_gatway.configuration.Constants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfiguration {

	@Value("${security.token-validation.mode:SIGNATURE}")
	private String tokenValidationMode;

	@Value("${security.adminUser}")
	private String adminUser;

	@Value("${security.adminPassword}")
	private String adminPassword;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private JwtIntrospector jwtIntrospector;

	@Autowired
	private MappingJwtGrantedAuthoritiesConverter mappingJwtGrantedAuthoritiesConverter;

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	SecurityWebFilterChain apiHttpSecurity(ServerHttpSecurity http) {
		ReactiveJwtAuthenticationConverter jwtAuthConverter = new ReactiveJwtAuthenticationConverter();
		jwtAuthConverter.setJwtGrantedAuthoritiesConverter(mappingJwtGrantedAuthoritiesConverter);

		http
			.authorizeExchange(exchanges -> exchanges
				.pathMatchers(Constants.GATEWAY_PROXY_PATH + "/**").authenticated()
				.pathMatchers("/access/**").hasRole("ADMIN")
				.pathMatchers("/routes/**").hasRole("USER")
				.pathMatchers(HttpMethod.GET, "/swagger-ui.html",
						"/webjars/swagger-ui/**", "/v3/api-docs*", "/v3/api-docs/**")
				.permitAll().pathMatchers("/apimgmt/**").permitAll())
			.securityMatcher(matcher ->
				ServerWebExchangeMatchers.matchers(
						ServerWebExchangeMatchers.pathMatchers(Constants.GATEWAY_PROXY_PATH + "/**"),
	                    ServerWebExchangeMatchers.pathMatchers("/access/**"),
	                    ServerWebExchangeMatchers.pathMatchers("/routes/**"),
	                ).matches(matcher)
			)
			.oauth2ResourceServer(oauth2 -> {
					if (tokenValidationMode.equals("INTROSPECTION")) {
						log.info("Using token introspection for token validation");
						oauth2.opaqueToken(token -> token.introspector(jwtIntrospector));
					} else {
						log.info("Using signature for token validation");
						oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthConverter));
					}
				}).csrf().disable().httpBasic();
//		http.httpBasic().and().authorizeExchange().anyExchange().permitAll().and().csrf().disable();
		return http.build();
	}

	@Bean
	public MapReactiveUserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder().username(adminUser).password(adminPassword)
				.roles("USER", "ADMIN")
				.build();
		return new MapReactiveUserDetailsService(user);
	}

	@Bean
	public Converter<Jwt, Flux<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
		return mappingJwtGrantedAuthoritiesConverter;
	}
}