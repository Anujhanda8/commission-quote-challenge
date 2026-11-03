package com.example.commissionquote.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.example.commissionquote.IntegrationTestBase;
import com.example.commissionquote.common.AppConstants.Headers;
import com.example.commissionquote.common.AppConstants.Paths;

class RequestLoggingFilterTest extends IntegrationTestBase {

	@Test
	void keepsSafeRequestIdFromCaller() {
		assertThat(RequestLoggingFilter.resolveRequestId("abc-123")).isEqualTo("abc-123");
	}

	@Test
	void replacesMissingOrUnsafeRequestId() {
		assertThat(RequestLoggingFilter.resolveRequestId(null)).hasSize(36);
		assertThat(RequestLoggingFilter.resolveRequestId("bad\nINFO fake log line")).hasSize(36);
		assertThat(RequestLoggingFilter.resolveRequestId("x".repeat(65))).hasSize(36);
	}

	@Test
	void returnsRequestIdHeaderOnApiResponses() {
		ResponseEntity<Map> response = client.post()
			.uri(Paths.QUOTES)
			.header(Headers.REQUEST_ID, "test-request-1")
			.body(loan(10000, 12, "LOW"))
			.retrieve()
			.toEntity(Map.class);

		assertThat(response.getHeaders().getFirst(Headers.REQUEST_ID)).isEqualTo("test-request-1");
	}
}
