package com.example.commissionquote;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import com.example.commissionquote.vendor.MockVendorProperties;

/**
 * Base class for integration tests: starts the full app (our API + mock vendor) on the port from
 * application-test.yml, so the vendor client calls the mock over real HTTP.
 *
 * All subclasses use the same configuration, so Spring starts the app once and shares it between them.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

	@LocalServerPort
	private int port;

	@Autowired
	protected MockVendorProperties mockVendorProperties;

	/** HTTP client for calling the app. Doesn't throw on 4xx/5xx, so tests can assert on error responses. */
	protected RestClient client;

	@BeforeEach
	void createClient() {
		client = RestClient.builder()
			.baseUrl("http://localhost:" + port)
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
			})
			.build();
	}

	protected static Map<String, Object> loan(Object amount, Object term, Object riskBand) {
		return Map.of("loanAmount", amount, "loanTermInMonths", term, "riskBand", riskBand);
	}
}
