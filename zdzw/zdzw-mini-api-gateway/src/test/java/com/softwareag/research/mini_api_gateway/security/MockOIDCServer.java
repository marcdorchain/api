/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gateway.security;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import lombok.Getter;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/apimgmt")
public class MockOIDCServer {

	@Getter
	private RSAKey rsaKey;

	private HttpHeaders jsonHeaders;

	public MockOIDCServer() throws NoSuchAlgorithmException, JOSEException {
		this.rsaKey = new RSAKeyGenerator(2048).generate();
		jsonHeaders = new HttpHeaders();
		jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
	}

	@GetMapping("/issuer")
	public Mono<ResponseEntity<String>> issuerInfo() throws JOSEException {
		return Mono.just(new ResponseEntity<String>(
				"{\"realm\":\"issuer\",\"public_key\":\"%s\",\"token-service\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect\",\"account-service\":\"http://localhost:8080/apimgmt/issuer/account\",\"tokens-not-before\":0}"
						.formatted(Base64.getEncoder().encodeToString(rsaKey.toPublicKey().getEncoded())),
				jsonHeaders, HttpStatus.OK));
	}

	@GetMapping("/issuer/.well-known/openid-configuration")
	public Mono<ResponseEntity<String>> oidcConfiguration() {
		return Mono.just(new ResponseEntity<String>(
				"{\"issuer\":\"http://localhost:8080/apimgmt/issuer\",\"authorization_endpoint\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/auth\",\"token_endpoint\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/token\",\"token_introspection_endpoint\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/token/introspect\",\"userinfo_endpoint\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/userinfo\",\"end_session_endpoint\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/logout\",\"jwks_uri\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/certs\",\"check_session_iframe\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/login-status-iframe.html\",\"grant_types_supported\":[\"authorization_code\",\"implicit\",\"refresh_token\",\"password\",\"client_credentials\"],\"response_types_supported\":[\"code\",\"none\",\"id_token\",\"token\",\"id_token token\",\"code id_token\",\"code token\",\"code id_token token\"],\"subject_types_supported\":[\"public\",\"pairwise\"],\"id_token_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\"],\"id_token_encryption_alg_values_supported\":[\"RSA-OAEP\",\"RSA1_5\"],\"id_token_encryption_enc_values_supported\":[\"A128GCM\",\"A128CBC-HS256\"],\"userinfo_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\",\"none\"],\"request_object_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\",\"none\"],\"response_modes_supported\":[\"query\",\"fragment\",\"form_post\"],\"registration_endpoint\":\"http://localhost:8080/apimgmt/issuer/clients-registrations/openid-connect\",\"token_endpoint_auth_methods_supported\":[\"private_key_jwt\",\"client_secret_basic\",\"client_secret_post\",\"tls_client_auth\",\"client_secret_jwt\"],\"token_endpoint_auth_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\"],\"claims_supported\":[\"aud\",\"sub\",\"iss\",\"auth_time\",\"jti\"],\"claim_types_supported\":[\"normal\"],\"claims_parameter_supported\":false,\"scopes_supported\":[\"openid\",\"offline_access\",\"profile\",\"email\",\"address\",\"phone\",\"roles\",\"web-origins\",\"microprofile-jwt\",\"ZDMP_Full_API_Access\",\"zappHelloWorld-scope\"],\"request_parameter_supported\":true,\"request_uri_parameter_supported\":true,\"code_challenge_methods_supported\":[\"plain\",\"S256\"],\"tls_client_certificate_bound_access_tokens\":true,\"introspection_endpoint\":\"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/token/introspect\"}",
				jsonHeaders, HttpStatus.OK));
	}

	@GetMapping("/issuer/protocol/openid-connect/certs")
	public Mono<Map<String, Object>> oidcCerts() {
		return Mono.just(new JWKSet(rsaKey).toJSONObject(true));
	}

	@PostMapping("/issuer/protocol/openid-connect/token/introspect")
	public Mono<Map<String, Boolean>> tokenIntrospection(@RequestParam(defaultValue = "true") boolean active) {
		return Mono.just(Map.of("active", active));
	}

}
