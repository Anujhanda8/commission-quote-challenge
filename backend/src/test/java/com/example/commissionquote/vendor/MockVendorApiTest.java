package com.example.commissionquote.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.example.commissionquote.IntegrationTestBase;
import com.example.commissionquote.common.AppConstants.ErrorMessages;
import com.example.commissionquote.common.AppConstants.Headers;
import com.example.commissionquote.common.AppConstants.Paths;

class MockVendorApiTest extends IntegrationTestBase {

	private static final String VENDOR_QUOTES = Paths.MOCK_VENDOR_BASE + Paths.MOCK_VENDOR_QUOTES;

	@Test
	void rejectsRequestWithoutApiKey() {
		ResponseEntity<Map> response = client.post()
			.uri(VENDOR_QUOTES)
			.body(loan(10000, 12, "LOW"))
			.retrieve()
			.toEntity(Map.class);

		assertThat(response.getStatusCode().value()).isEqualTo(401);
	}

	@Test
	void rejectsRequestWithWrongApiKey() {
		ResponseEntity<Map> response = postWithKey("wrong-key", loan(10000, 12, "LOW"));

		assertThat(response.getStatusCode().value()).isEqualTo(401);
	}

	@Test
	void returnsQuoteForValidApiKey() {
		ResponseEntity<Map> response = postWithKey(mockVendorProperties.apiKey(), loan(10000, 12, "HIGH"));

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).containsKeys("quoteId", "commissionRate", "totalCommission");
		assertThat(response.getBody().get("totalCommission")).isEqualTo(200.0);
	}

	@Test
	void errorScenarioAmountAlwaysFails() {
		ResponseEntity<Map> response = postWithKey(mockVendorProperties.apiKey(), loan(mockVendorProperties.scenarios().errorAmount(), 12, "LOW"));

		assertThat(response.getStatusCode().value()).isEqualTo(500);
	}

	@Test
	void rejectsInvalidCharactersInTheBody() {
		ResponseEntity<Map> response = postWithKey(mockVendorProperties.apiKey(), loan(10000, 12, "<script>"));

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat(response.getBody().get("message")).isEqualTo(ErrorMessages.INVALID_CHARACTERS);
	}

	private ResponseEntity<Map> postWithKey(String apiKey, Map<String, Object> body) {
		return client.post()
			.uri(VENDOR_QUOTES)
			.header(Headers.API_KEY, apiKey)
			.body(body)
			.retrieve()
			.toEntity(Map.class);
	}
}
