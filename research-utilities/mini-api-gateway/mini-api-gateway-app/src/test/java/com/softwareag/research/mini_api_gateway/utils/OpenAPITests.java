package com.softwareag.research.mini_api_gateway.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.softwareag.research.mini_api_gatway.utils.APISpecificationUtils;

import io.swagger.v3.oas.models.OpenAPI;

public class OpenAPITests {

	@Test
	public void validOpenAPIJson() throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("petstore_openapi.json");
		String specification = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		OpenAPI openAPI = APISpecificationUtils.parseAPISpecification(specification);
		assertNotNull(openAPI);
		assertEquals("Swagger Petstore - OpenAPI 3.0", openAPI.getInfo().getTitle());
	}

	@Test
	public void validOpenAPIYAML() throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("petstore_openapi.yaml");
		String specification = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		OpenAPI openAPI = APISpecificationUtils.parseAPISpecification(specification);
		assertNotNull(openAPI);
		assertEquals("Swagger Petstore - OpenAPI 3.0", openAPI.getInfo().getTitle());
	}

	@Test
	public void parsedOpenAPIIsValid() throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("petstore_openapi.yaml");
		String specification = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		OpenAPI openAPI = APISpecificationUtils.parseAPISpecification(specification);
		assertNotNull(openAPI);
		specification = APISpecificationUtils.openAPItoJSON(openAPI);
		openAPI = APISpecificationUtils.parseAPISpecification(specification);
		assertEquals("Swagger Petstore - OpenAPI 3.0", openAPI.getInfo().getTitle());
	}

}
