/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Replaces JSON-Path style expressions with values from maps containing any
 * object or JSON type <br>
 * Example: The expression <code>${response.headers.Authorization}</code> is
 * replaced with the value from the map key <code>response</code>'s child
 * attribute <code>headers</code>' child attribute <code>Authorization</code>.
 *
 *
 * @author jonsch
 *
 */
public class VariableExpressionResolver {

	private static final Pattern VAR_PATTERN = Pattern.compile("[$][{]([^}]+)}");
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
	private static final String UNDEFINED = "undefined";
	private static final char SLASH = '/';
	private ObjectMapper mapper;

	public VariableExpressionResolver() {
		mapper = new ObjectMapper();
	}

	public String format(String template, Map<String, Object> parameters) {
		StringBuilder newTemplate = new StringBuilder(template);
		LinkedList<String> valueList = new LinkedList<>();

		Matcher matcher = VAR_PATTERN.matcher(template);

		while (matcher.find()) {
			String key = matcher.group(1);

			String paramName = "${" + key + "}";
			int index = newTemplate.indexOf(paramName);
			if (index != -1) {
				newTemplate.replace(index, index + paramName.length(), "%s");
				try {
					String[] splitPath = DOT_PATTERN.split(key, 2);
					JsonNode tree = mapper.valueToTree(parameters.get(splitPath[0]));
					JsonNode node = tree.at(SLASH + splitPath[1].replace('.', SLASH));
					if (node.isValueNode()) {
						valueList.add(node.asText());
					} else if (node.isMissingNode() && splitPath.length > 1) {
						splitPath = DOT_PATTERN.split(splitPath[1], 2);
						String nodeValue;
						if (tree.isArray()) {
							nodeValue = tree.get(Integer.parseInt(splitPath[0])).asText();
						} else {
							nodeValue = tree.get(splitPath[0]).asText();
						}
						valueList.add(resolvePath(nodeValue, splitPath[1]));
					} else {
						throw new NoSuchElementException();
					}
				} catch (RuntimeException | JsonProcessingException e) {
					valueList.add(UNDEFINED);
				}
			}
		}

		return String.format(newTemplate.toString(), valueList.toArray());
	}

	private String resolvePath(String input, String path) throws JsonMappingException, JsonProcessingException {
		if (input.isEmpty()) {
			throw new NoSuchElementException();
		}
		JsonNode tree = mapper.readTree(input);
		JsonNode node = tree.at(SLASH + path.replace('.', SLASH));
		if (node.isValueNode()) {
			return node.asText();
		} else if (node.isMissingNode()) {
			String[] splitPath = DOT_PATTERN.split(path, 2);
			if (tree.isArray()) {
				return resolvePath(tree.get(Integer.parseInt(splitPath[0])).asText(), splitPath[1]);
			} else {
				return resolvePath(tree.get(splitPath[0]).asText(), splitPath[1]);
			}
		} else {
			throw new NoSuchElementException();
		}
	}

}
