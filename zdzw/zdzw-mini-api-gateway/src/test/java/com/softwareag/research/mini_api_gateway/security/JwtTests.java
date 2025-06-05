/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gateway.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.softwareag.research.mini_api_gatway.MiniAPIGWApplication;
import com.softwareag.research.mini_api_gatway.exceptions.IssuerValidationException;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer.IssuerMatchMode;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuerRepository;
import com.softwareag.research.mini_api_gatway.security.IssuerValidationService;
import com.softwareag.research.mini_api_gatway.security.JwtDecoder;
import com.softwareag.research.mini_api_gatway.security.JwtIntrospector;

@SpringBootTest(classes = { MiniAPIGWApplication.class,
		MockOIDCServer.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
public class JwtTests {

	@Autowired
	private OpenIDConnectIssuerRepository oidcRepo;

	@Autowired
	private IssuerValidationService issuerValidationService;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private JwtIntrospector jwtIntrospector;

	@Autowired
	private MockOIDCServer mockOIDCServer;

	@BeforeEach
	public void clearOIDCRepo() {
		oidcRepo.deleteAll().block();
	}

	@Test
	public void validIssuerTestFullMatch() {
		OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(null, "https://jwt.io", null, IssuerMatchMode.FULL_MATCH,
				null, null);
		issuer = oidcRepo.save(issuer).block();
		issuerValidationService.refreshIssuerCache().block();
		OpenIDConnectIssuer resultIssuer = issuerValidationService.validateOidcIssuer("https://jwt.io");
		assertEquals(issuer, resultIssuer);
	}

	@Test
	public void validIssuerTestPrefixMatch() {
		OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(null, "https://jwt.io/", null,
				IssuerMatchMode.PREFIX_MATCH, null, null);
		issuer = oidcRepo.save(issuer).block();
		issuerValidationService.refreshIssuerCache().block();
		OpenIDConnectIssuer resultIssuer = issuerValidationService.validateOidcIssuer("https://jwt.io/some_issuer");
		assertEquals(issuer, resultIssuer);
	}

	@Test
	public void defaultIssuerTest() {
		OpenIDConnectIssuer resultIssuer = issuerValidationService
				.validateOidcIssuer("https://testserver.com/issuer_a");
		assertNotNull(resultIssuer);
	}

	@Test
	public void missingIssuerTest() {
		assertThrows(IssuerValidationException.class, () -> {
			issuerValidationService.validateOidcIssuer("https://example.com");
		});
	}

	@Test
	public void IssuerTestNoPrefixMatch() {
		assertThrows(IssuerValidationException.class, () -> {
			OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(null, "https://jwt.io/trusted_issuers/", null,
					IssuerMatchMode.PREFIX_MATCH, null, null);
			issuer = oidcRepo.save(issuer).block();
			issuerValidationService.refreshIssuerCache().block();
			issuerValidationService.validateOidcIssuer("https://jwt.io/untrusted_issuers/issuer_a");
		});
	}

	@Test
	public void validTokenSignature() throws JOSEException {
		OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(null, "http://localhost:8080/apimgmt/issuer", null,
				IssuerMatchMode.FULL_MATCH, null, null);
		issuer = oidcRepo.save(issuer).block();
		issuerValidationService.refreshIssuerCache().block();
		String token = new PrivateKeyJWT(new Issuer("http://localhost:8080/apimgmt/issuer"), new ClientID("my-client"),
				URI.create("http://localhost:8080/apimgmt/issuer/token"), JWSAlgorithm.RS256,
				mockOIDCServer.getRsaKey().toRSAPrivateKey(), mockOIDCServer.getRsaKey().getKeyID(), null)
				.getClientAssertion().serialize();
		jwtDecoder.decode(token).block();
	}

	@Test
	public void invalidTokenSignature() throws JOSEException {
		OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(null, "http://localhost:8080/apimgmt/issuer", null,
				IssuerMatchMode.FULL_MATCH, null, null);
		issuer = oidcRepo.save(issuer).block();
		issuerValidationService.refreshIssuerCache().block();
		assertThrows(JwtException.class, () -> {
			String token = new PrivateKeyJWT(new Issuer("http://localhost:8080/apimgmt/issuer"),
					new ClientID("my-client"), URI.create("http://localhost:8080/apimgmt/issuer/token"),
					JWSAlgorithm.RS256, new RSAKeyGenerator(2048).generate().toPrivateKey(),
					mockOIDCServer.getRsaKey().getKeyID(), null).getClientAssertion().serialize();
			jwtDecoder.decode(token).block();
		});
	}

	@Test
	public void validTokenIntrospection() throws JOSEException {
		OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(null, "http://localhost:8080/apimgmt/issuer",
				"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/token/introspect",
				IssuerMatchMode.FULL_MATCH, null, null);
		issuer = oidcRepo.save(issuer).block();
		issuerValidationService.refreshIssuerCache().block();
		String token = new PrivateKeyJWT(new Issuer("http://localhost:8080/apimgmt/issuer"), new ClientID("my-client"),
				URI.create("http://localhost:8080/apimgmt/issuer/token"), JWSAlgorithm.RS256,
				mockOIDCServer.getRsaKey().toRSAPrivateKey(), mockOIDCServer.getRsaKey().getKeyID(), null)
				.getClientAssertion().serialize();
		jwtIntrospector.introspect(token).block();
	}

	@Test
	public void inactiveTokenIntrospection() throws JOSEException {
		OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(null, "http://localhost:8080/apimgmt/issuer",
				"http://localhost:8080/apimgmt/issuer/protocol/openid-connect/token/introspect?active=false",
				IssuerMatchMode.FULL_MATCH, null, null);
		issuer = oidcRepo.save(issuer).block();
		issuerValidationService.refreshIssuerCache().block();
		assertThrows(BadOpaqueTokenException.class, () -> {
			String token = new PrivateKeyJWT(new Issuer("http://localhost:8080/apimgmt/issuer"),
					new ClientID("my-client"), URI.create("http://localhost:8080/apimgmt/issuer/token"),
					JWSAlgorithm.RS256, mockOIDCServer.getRsaKey().toRSAPrivateKey(),
					mockOIDCServer.getRsaKey().getKeyID(), null).getClientAssertion().serialize();
			jwtIntrospector.introspect(token).block();
		});
	}

}
