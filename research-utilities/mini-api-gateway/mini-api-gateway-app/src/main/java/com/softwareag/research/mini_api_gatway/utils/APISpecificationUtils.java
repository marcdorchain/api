/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.utils;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods for parsing and converting OpenAPI specifications
 *
 * @author jonsch
 *
 */
@Slf4j
public class APISpecificationUtils {

	public static ParseOptions parseOptions = createParseOptions();
	public static ObjectMapper objectMapper = setupObjectMapper();

	private static ObjectMapper setupObjectMapper() {
		return new ObjectMapper().setDefaultPropertyInclusion(Include.NON_EMPTY)
				.addMixIn(io.swagger.v3.oas.models.media.MediaType.class, OpenAPIModelIgnoreMixIn.class)
				.addMixIn(io.swagger.v3.oas.models.media.Schema.class, OpenAPIModelIgnoreMixIn.class)
				.addMixIn(io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.class, OpenAPIEnumMixIn.class)
				.addMixIn(io.swagger.v3.oas.models.security.SecurityScheme.Type.class, OpenAPIEnumMixIn.class)
				.addMixIn(io.swagger.v3.oas.models.security.SecurityScheme.In.class, OpenAPIEnumMixIn.class)
				.addMixIn(io.swagger.v3.oas.models.headers.Header.StyleEnum.class, OpenAPIEnumMixIn.class);

	}

	private static abstract class OpenAPIModelIgnoreMixIn {
		@JsonIgnore
		boolean exampleSetFlag;

		@JsonIgnore
		abstract Set<String> getTypes();
	}

	private static abstract class OpenAPIEnumMixIn {
		@JsonValue
		private String value;
	}

	private static ParseOptions createParseOptions() {
		ParseOptions parseOptions = new ParseOptions();
		parseOptions.setAllowEmptyString(true);
		parseOptions.setValidateInternalRefs(false);
		parseOptions.setValidateExternalRefs(false);
		parseOptions.setResolve(false);
		return parseOptions;
	}

	public static OpenAPI parseAPISpecification(String specification) {
		// System.out.println(specification);
		SwaggerParseResult result = new OpenAPIParser().readContents(specification, null, parseOptions);
		for (String message : result.getMessages()) {
			log.error(message);
		}
		return result.getOpenAPI();
	}

	public static String openAPItoJSON(OpenAPI api) throws JsonProcessingException {
		return objectMapper.writeValueAsString(api);
	}

}
