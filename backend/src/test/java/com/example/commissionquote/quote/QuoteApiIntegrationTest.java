package com.example.commissionquote.quote;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.example.commissionquote.IntegrationTestBase;
import com.example.commissionquote.common.AppConstants.ErrorMessages;
import com.example.commissionquote.common.error.ErrorCode;
import com.example.commissionquote.common.AppConstants.Paths;

class QuoteApiIntegrationTest extends IntegrationTestBase {

	@Test
	void returnsQuoteFromVendor() {
		ResponseEntity<Map> response = postQuote(loan(100000, 72, "MEDIUM"));

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody().get("quoteId")).isNotNull();
		assertThat(response.getBody().get("commissionRate")).isEqualTo(1.75);
		assertThat(response.getBody().get("totalCommission")).isEqualTo(1750.0);
	}

	@Test
	void rejectsOutOfRangeValuesWithFieldErrors() {
		ResponseEntity<Map> response = postQuote(loan(500, 0, "LOW"));

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
		assertThat(fieldErrors).containsKeys("loanAmount", "loanTermInMonths");
	}

	@Test
	void rejectsTooManyDecimalPlaces() {
		ResponseEntity<Map> response = postQuote(loan(1000.123, 12, "LOW"));

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat((Map<String, String>) response.getBody().get("fieldErrors")).containsKey("loanAmount");
	}

	@Test
	void rejectsUnknownRiskBand() {
		ResponseEntity<Map> response = postQuote(loan(10000, 12, "EXTREME"));

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat(response.getBody().get("message")).isEqualTo(ErrorMessages.INVALID_CHARACTERS);
		assertThat(response.getBody().get("code")).isEqualTo(ErrorCode.INVALID_CHARACTERS.code());
	}

	@Test
	void returnsBadGatewayWhenVendorFails() {
		ResponseEntity<Map> response = postQuote(loan(mockVendorProperties.scenarios().errorAmount(), 12, "LOW"));

		assertThat(response.getStatusCode().value()).isEqualTo(502);
		assertThat(response.getBody().get("message")).isEqualTo(ErrorMessages.VENDOR_ERROR);
		assertThat(response.getBody().get("code")).isEqualTo(ErrorCode.VENDOR_ERROR.code());
	}

	@Test
	void returnsGatewayTimeoutWhenVendorIsSlow() {
		ResponseEntity<Map> response = postQuote(loan(mockVendorProperties.scenarios().timeoutAmount(), 12, "LOW"));

		assertThat(response.getStatusCode().value()).isEqualTo(504);
		assertThat(response.getBody().get("message")).isEqualTo(ErrorMessages.VENDOR_TIMEOUT);
		assertThat(response.getBody().get("code")).isEqualTo(ErrorCode.VENDOR_TIMEOUT.code());
	}

	private ResponseEntity<Map> postQuote(Map<String, Object> body) {
		return client.post().uri(Paths.QUOTES).body(body).retrieve().toEntity(Map.class);
	}
}
