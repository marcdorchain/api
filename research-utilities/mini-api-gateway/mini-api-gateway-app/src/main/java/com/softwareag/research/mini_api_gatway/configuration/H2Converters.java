/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.configuration;

import java.io.IOException;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.H2Dialect;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.research.mini_api_gatway.model.RoutePath;
import com.softwareag.research.mini_api_gatway.model.RouteSettings;
import com.softwareag.research.mini_api_gatway.model.filters.ExternalCallFilter;
import com.softwareag.research.mini_api_gatway.model.filters.OAuth2ClientCredentialsFilter;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilter;
import com.softwareag.research.mini_api_gatway.model.filters.SetHeadersFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom object to database type converters for H2 database. Converts
 * {@link RouteFilter} to JSON Strings and vice-versa.
 *
 * @author jonsch
 *
 */
@Configuration
@Slf4j
public class H2Converters {

	private final ObjectMapper objectMapper = new ObjectMapper().setVisibility(PropertyAccessor.ALL, Visibility.ANY);

	@Profile("h2")
	@Bean
	public R2dbcCustomConversions customConversions() {
		return R2dbcCustomConversions.of(H2Dialect.INSTANCE, new JsonToRouteFilterConverter(objectMapper));
	}

	static class JsonToRouteFilterConverter implements GenericConverter {

		private final ObjectMapper objectMapper;

		public JsonToRouteFilterConverter(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Set.of(
					new ConvertiblePair(RouteFilter.class, String.class),
					new ConvertiblePair(ExternalCallFilter.class, String.class),
					new ConvertiblePair(SetHeadersFilter.class, String.class),
					new ConvertiblePair(OAuth2ClientCredentialsFilter.class, String.class),
					new ConvertiblePair(RouteFilter[].class, String.class),
					new ConvertiblePair(String.class, RouteFilter.class),
					new ConvertiblePair(String.class, RouteFilter[].class),
					new ConvertiblePair(String.class, String[].class),
					new ConvertiblePair(String[].class, String.class),
					new ConvertiblePair(RoutePath[].class, String.class),
					new ConvertiblePair(String.class, RoutePath[].class),
					new ConvertiblePair(RouteSettings.class, String.class),
					new ConvertiblePair(String.class, RouteSettings.class)
			);
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			// log.info(targetType.toString());
			try {
				if(source instanceof String) {
					return objectMapper.readValue((String) source, targetType.getType());
				}else {
					return objectMapper.writeValueAsString(source);
				}
			}catch(IOException e) {
				log.error("Failed to convert to/from JSON", e);
				return null;
			}
		}
	}
}
