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
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.research.mini_api_gatway.model.RoutePath;
import com.softwareag.research.mini_api_gatway.model.RouteSettings;
import com.softwareag.research.mini_api_gatway.model.filters.ExternalCallFilter;
import com.softwareag.research.mini_api_gatway.model.filters.OAuth2ClientCredentialsFilter;
import com.softwareag.research.mini_api_gatway.model.filters.RouteFilter;
import com.softwareag.research.mini_api_gatway.model.filters.SetHeadersFilter;

import io.r2dbc.postgresql.codec.Json;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom object to database type converters for PostgreSQL database. Converts
 * {@link RouteFilter} to JSON Strings and vice-versa.
 *
 * @author jonsch
 *
 */
@Configuration
@Slf4j
public class PostgresConverters {

	private final ObjectMapper objectMapper = new ObjectMapper().setVisibility(PropertyAccessor.ALL, Visibility.ANY);

	@Profile("postgres")
	@Bean
	public R2dbcCustomConversions customConversions() {
		return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, new JsonToRouteFilterConverter(objectMapper));
	}

	static class JsonToStringConverter implements GenericConverter {

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Set.of(new ConvertiblePair(String.class, Json.class), new ConvertiblePair(Json.class, String.class));
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (source instanceof Json) {
				return ((Json) source).asString();
			} else {
				return Json.of((String) source);
			}
		}

	}

	static class JsonToRouteFilterConverter implements GenericConverter {

		private final ObjectMapper objectMapper;

		public JsonToRouteFilterConverter(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Set.of(
					new ConvertiblePair(RouteFilter.class, Json.class),
					new ConvertiblePair(ExternalCallFilter.class, Json.class),
					new ConvertiblePair(SetHeadersFilter.class, Json.class),
					new ConvertiblePair(OAuth2ClientCredentialsFilter.class, Json.class),
					new ConvertiblePair(RouteFilter[].class, Json.class),
					new ConvertiblePair(Json.class, RouteFilter.class),
					new ConvertiblePair(Json.class, RouteFilter[].class),
					new ConvertiblePair(String.class, RouteFilter.class),
					new ConvertiblePair(String.class, ExternalCallFilter.class),
					new ConvertiblePair(Json.class, String[].class),
					new ConvertiblePair(String[].class, Json.class),
					new ConvertiblePair(RoutePath[].class, Json.class),
					new ConvertiblePair(Json.class, RoutePath[].class),
					new ConvertiblePair(RouteSettings.class, Json.class),
					new ConvertiblePair(Json.class, RouteSettings.class)
			);
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			// log.info(targetType.toString());
			try {
				if (source instanceof Json) {
					return objectMapper.readValue(((Json) source).asArray(), targetType.getType());
				} else if (source instanceof String) {
					return objectMapper.readValue((String) source, targetType.getType());
				}else {
					return Json.of(objectMapper.writeValueAsBytes(source));
				}
			}catch(IOException e) {
				log.error("Failed to convert to/from JSON", e);
				return null;
			}
		}
	}
}
