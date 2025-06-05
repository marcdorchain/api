package com.softwareag.research.mini_api_gateway.utils;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.softwareag.research.mini_api_gatway.utils.JwtUtils;

public class JwtUtilsTests {

	@Test
	public void signedTokenTest() {
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJpc3MiOiJodHRwczovL2p3dC5pbyJ9.YjqhO845G2mQBajYF-2zcEesKm1XnzJ8gGbZ2AS1G3Q";
		String issuer = JwtUtils.getTokenIssuer(token);
		assertEquals(issuer, "https://jwt.io");
	}
}
