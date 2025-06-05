package com.softwareag.research.mini_api_gateway.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.softwareag.research.mini_api_gatway.MiniAPIGWApplication;
import com.softwareag.research.mini_api_gatway.api.request.OpenIDConnectIssuerRequest;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuer.IssuerMatchMode;
import com.softwareag.research.mini_api_gatway.model.OpenIDConnectIssuerRepository;

import jakarta.annotation.PostConstruct;

@SpringBootTest(classes = { MiniAPIGWApplication.class }, webEnvironment = WebEnvironment.MOCK)
public class AccessMgmtTests {

	@Autowired
	private ApplicationContext appContext;

	@Autowired
	private OpenIDConnectIssuerRepository oidcRepo;

	private WebTestClient webClient;

	@PostConstruct
	private void setup() {
		webClient = WebTestClient.bindToApplicationContext(appContext)
				.configureClient()
				.baseUrl("/access/oidc-issuers")
				.defaultHeaders(headers -> headers.setBasicAuth("admin", "admin"))
				.build();
	}

	@Test
	public void emptyListTest() {
		oidcRepo.deleteAll().block();
		webClient
			.get()
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().is2xxSuccessful()
			.expectBodyList(OpenIDConnectIssuer.class)
			.hasSize(0);
	}

	@Test
	public void listTest() {
		OpenIDConnectIssuer issuer = new OpenIDConnectIssuer(null, "https://issuer0.com", null,
				IssuerMatchMode.FULL_MATCH, null, null);
		issuer = oidcRepo.save(issuer).block();
		webClient
			.get()
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().is2xxSuccessful()
			.expectBodyList(OpenIDConnectIssuer.class)
			.contains(issuer);
	}

	@Test
	public void createIssuerTest() {
		OpenIDConnectIssuerRequest body = new OpenIDConnectIssuerRequest("https://issuer1.com", IssuerMatchMode.FULL_MATCH);
		EntityExchangeResult<OpenIDConnectIssuer> result = webClient
			.post()
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(OpenIDConnectIssuer.class)
			.returnResult();
		webClient
			.get()
			.uri("/{id}", result.getResponseBody().getId())
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectBody(OpenIDConnectIssuer.class)
			.isEqualTo(result.getResponseBody());
	}

	@Test
	public void duplicateIssuerTest() {
		OpenIDConnectIssuerRequest body = new OpenIDConnectIssuerRequest("https://issuer2.com", IssuerMatchMode.FULL_MATCH);
		webClient
			.post()
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(OpenIDConnectIssuer.class);
		webClient
			.post()
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	public void deleteIssuerTest() {
		OpenIDConnectIssuerRequest body = new OpenIDConnectIssuerRequest("https://issuer3.com", IssuerMatchMode.FULL_MATCH);
		EntityExchangeResult<OpenIDConnectIssuer> result = webClient
			.post()
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(OpenIDConnectIssuer.class)
			.returnResult();
		webClient
			.delete()
			.uri("/{id}", result.getResponseBody().getId())
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk();
	}
}
