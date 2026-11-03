package com.example.commissionquote.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import com.example.commissionquote.common.AppConstants.Paths;

/**
 * With the mock off (the default), the app starts without any mock-vendor settings and /vendor doesn't exist,
 * so a real deployment can't expose the mock by accident.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "vendor.api-key=real-key", "mock-vendor.enabled=false" })
class MockVendorDisabledTest {

	@LocalServerPort
	private int port;

	@Autowired
	private ApplicationContext context;

	@Test
	void mockVendorBeansAreNotCreated() {
		assertThat(context.getBeanNamesForType(MockVendorController.class)).isEmpty();
		assertThat(context.getBeanNamesForType(VendorApiKeyFilter.class)).isEmpty();
		assertThat(context.getBeanNamesForType(MockVendorProperties.class)).isEmpty();
	}

	@Test
	void vendorEndpointReturnsNotFound() {
		int status = RestClient.create("http://localhost:" + port)
			.post()
			.uri(Paths.MOCK_VENDOR_BASE + Paths.MOCK_VENDOR_QUOTES)
			.header("Content-Type", "application/json")
			.body("{}")
			.retrieve()
			.onStatus(HttpStatusCode::isError, (request, response) -> {
			})
			.toBodilessEntity()
			.getStatusCode()
			.value();

		assertThat(status).isEqualTo(404);
	}
}
