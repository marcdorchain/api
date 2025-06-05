/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.model.filters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.softwareag.research.mini_api_gatway.model.RouteEntity;

import io.swagger.v3.oas.models.PathItem.HttpMethod;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public class RoutePathValidatorFilter implements GatewayFilter {

	// Bit-Flags for allowed HTTP methods
	private static final int METHOD_DELETE = 1;
	private static final int METHOD_GET = 2;
	private static final int METHOD_HEAD = 4;
	private static final int METHOD_OPTIONS = 8;
	private static final int METHOD_PATCH = 16;
	private static final int METHOD_POST = 32;
	private static final int METHOD_PUT = 64;
	private static final int METHOD_TRACE = 128;

	// Special Strings for path tree maps
	private static final String ROOT_PLACEHOLDER = "<root>";
	private static final String WILDCARD_PLACEHOLDER = "<*>";

	private Map<String, Object> pathMap = new HashMap<>();
	private final int basePathLength;
	private final boolean allowHEAD;
	private final boolean allowOPTIONS;

	/**
	 *
	 * Runs at setup (low time-critical)
	 *
	 * @param route
	 */
	public RoutePathValidatorFilter(RouteEntity route) {

		if (route.getPaths() == null) {
			throw new IllegalArgumentException("Route must have paths defined to be validated");
		}

		try {
			basePathLength = new URL(route.getEndpoint()).getPath().length();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		allowHEAD = route.getSettings().isAlwaysAllowHEAD();
		allowOPTIONS = route.getSettings().isAlwaysAllowOPTIONS();

		for (var path : route.getPaths()) {
			if (path.isActive()) {
				String[] pathSegments = path.getPath().split("/");
				insertPath(pathSegments, 0, pathMap, path.getMethod());

			}
		}
	}

	/**
	 *
	 * Runs at setup (low time-critical)
	 *
	 * @param pathSegments
	 * @param index
	 * @param map
	 * @param method
	 */
	@SuppressWarnings("unchecked")
	private void insertPath(String[] pathSegments, int index, Map<String, Object> map, HttpMethod method) {
		if (index == pathSegments.length) {
			int flags = (int) map.getOrDefault(ROOT_PLACEHOLDER, 0);
			map.put(ROOT_PLACEHOLDER, flags | mapMethod(method));
//		} else if(index == pathSegments.length-1) {
//			int flags = (int) map.getOrDefault(pathSegments[index], 0);
//			map.put(pathSegments[index], addMethod(flags, method));
		} else {
			if (index == 0 && pathSegments[index].length() == 0) {
				insertPath(pathSegments, index + 1, map, method);
			} else {
				Map<String, Object> nMap;
				if (pathSegments[index].charAt(0) == '{') {
					nMap = (Map<String, Object>) map.getOrDefault(WILDCARD_PLACEHOLDER, new HashMap<String, Object>());
					map.put(WILDCARD_PLACEHOLDER, nMap);
				} else {
					nMap = (Map<String, Object>) map.getOrDefault(pathSegments[index], new HashMap<String, Object>());
					map.put(pathSegments[index], nMap);
				}
				insertPath(pathSegments, index + 1, nMap, method);
			}
		}
	}

	/**
	 *
	 * Runs at setup (low time-critical)
	 *
	 * @param method
	 * @return
	 */
	private int mapMethod(HttpMethod method) {
		switch (method) {
		case DELETE:
			return METHOD_DELETE;
		case GET:
			return METHOD_GET;
		case HEAD:
			return METHOD_HEAD;
		case OPTIONS:
			return METHOD_OPTIONS;
		case PATCH:
			return METHOD_PATCH;
		case POST:
			return METHOD_POST;
		case PUT:
			return METHOD_PUT;
		case TRACE:
			return METHOD_TRACE;
		default:
			log.warn("Unsupported HTTP Method");
			return 0;
		}
	}

	/**
	 *
	 * Runs on request (high time-critical)
	 *
	 * @param method
	 * @return
	 */
	private int mapMethod(org.springframework.http.HttpMethod method) {
		switch (method.toString()) {
		case "GET":
			return METHOD_GET;
		case "HEAD":
			return METHOD_HEAD;
		case "POST":
			return METHOD_POST;
		case "PUT":
			return METHOD_PUT;
		case "PATCH":
			return METHOD_PATCH;
		case "DELETE":
			return METHOD_DELETE;
		case "OPTIONS":
			return METHOD_OPTIONS;
		case "TRACE":
			return METHOD_TRACE;
		default:
			log.warn("Unsupported HTTP Method");
			return 0;
		}
	}

	/**
	 *
	 * Runs on request (high time-critical)
	 *
	 * @param pathSegments
	 * @param index
	 * @param map
	 * @param methodFlag
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean isAllowed(String[] pathSegments, int index, Map<String, Object> map, int methodFlag) {
		if (index == pathSegments.length) {
			int flags = (int) map.getOrDefault(ROOT_PLACEHOLDER, 0);
			// log.info("<root>, flags = %d , method = %d".formatted(flags, methodFlag));
			return (flags & methodFlag) != 0;
		} else {
			if (index == 0 && pathSegments[index].length() == 0) {
				return isAllowed(pathSegments, index + 1, map, methodFlag);
			} else {
				Map<String, Object> nMap = (Map<String, Object>) map.get(pathSegments[index]);
				if (nMap == null) {
					nMap = (Map<String, Object>) map.get(WILDCARD_PLACEHOLDER);
					if (nMap == null) {
						return false;
					} else {
						return isAllowed(pathSegments, index + 1, nMap, methodFlag);
					}
				} else {
					return isAllowed(pathSegments, index + 1, nMap, methodFlag);
				}
			}
		}
	}

	/**
	 *
	 * Runs on request (high time-critical)
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String[] pathSegments = exchange.getRequest().getPath().value().substring(basePathLength).split("/");
		var method = exchange.getRequest().getMethod();
		if ((method.equals(org.springframework.http.HttpMethod.HEAD) && allowHEAD)
				|| (method.equals(org.springframework.http.HttpMethod.OPTIONS) && allowOPTIONS)
				|| isAllowed(pathSegments, 0, pathMap, mapMethod(method))) {
			return chain.filter(exchange);
		} else {
			throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
		}

	}

	private static class WilcardMap<K, V> implements Map<K, V> {

		private V rootValue;
		private V subValue;

		@Override
		public int size() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEmpty() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsKey(Object key) {
			if (key.equals(ROOT_PLACEHOLDER)) {
				return rootValue != null;
			} else {
				return subValue != null;
			}
		}

		@Override
		public boolean containsValue(Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V get(Object key) {
			if (key.equals(ROOT_PLACEHOLDER)) {
				return rootValue;
			} else {
				return subValue;
			}
		}

		@Override
		public V put(K key, V value) {
			V previousValue;
			if (key.equals(ROOT_PLACEHOLDER)) {
				previousValue = rootValue;
				rootValue = value;
			} else {
				previousValue = subValue;
				subValue = value;
			}
			return previousValue;
		}

		@Override
		public V remove(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			this.rootValue = null;
			this.subValue = null;
		}

		@Override
		public Set<K> keySet() {
			return Set.of();
		}

		@Override
		public Collection<V> values() {
			ArrayList<V> list = new ArrayList<>();
			list.add(rootValue);
			list.add(subValue);
			return list;
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			throw new UnsupportedOperationException();
		}

	}

}
