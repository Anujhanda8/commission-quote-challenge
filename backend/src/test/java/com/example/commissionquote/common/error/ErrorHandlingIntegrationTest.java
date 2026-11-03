package com.example.commissionquote.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.commissionquote.IntegrationTestBase;
import com.example.commissionquote.common.AppConstants.ErrorMessages;
import com.example.commissionquote.common.AppConstants.Headers;
import com.example.commissionquote.common.AppConstants.SecurityHeaders;
import com.example.commissionquote.common.AppConstants.ValidationMessages;
import com.example.commissionquote.common.AppConstants.Paths;

class ErrorHandlingIntegrationTest extends IntegrationTestBase {

	@Test
	void malformedJsonReturnsBadRequest() {
		ResponseEntity<Map> response = client.post()
			.uri(Paths.QUOTES)
			.contentType(MediaType.APPLICATION_JSON)
			.body("{not json")
			.retrieve()
			.toEntity(Map.class);

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat(response.getBody().get("message")).isEqualTo(ErrorMessages.UNREADABLE_BODY);
	}

	@Test
	void missingFieldsReturnFieldErrors() {
		ResponseEntity<Map> response = client.post()
			.uri(Paths.QUOTES)
			.contentType(MediaType.APPLICATION_JSON)
			.body("{}")
			.retrieve()
			.toEntity(Map.class);

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat((Map<String, String>) response.getBody().get("fieldErrors"))
			.containsKeys("loanAmount", "loanTermInMonths", "riskBand");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{\"loanAmount\":\"abc\",\"loanTermInMonths\":12,\"riskBand\":\"LOW\"}",
			"{\"loanAmount\":\"<script>alert(1)</script>\",\"loanTermInMonths\":12,\"riskBand\":\"LOW\"}",
			"{\"loanAmount\":-5000,\"loanTermInMonths\":12,\"riskBand\":\"LOW\"}",
			"{\"loanAmount\":1e5,\"loanTermInMonths\":12,\"riskBand\":\"LOW\"}",
			"{\"loanAmount\":10000,\"loanTermInMonths\":\"twelve\",\"riskBand\":\"LOW\"}",
			"{\"loanAmount\":10000,\"loanTermInMonths\":[12],\"riskBand\":\"LOW\"}",
			"{\"loanAmount\":10000,\"loanTermInMonths\":12,\"riskBand\":\"EXTREME\"}",
			"{\"loanAmount\":10000,\"loanTermInMonths\":12,\"riskBand\":\"low\"}",
			"{\"loanAmount\":10000,\"loanTermInMonths\":12,\"riskBand\":\"<img src=x onerror=alert(1)>\"}" })
	void invalidCharactersAreRejectedWithAGenericMessage(String json) {
		ResponseEntity<Map> response = postRaw(json);

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat(response.getBody().get("message")).isEqualTo(ErrorMessages.INVALID_CHARACTERS);
		assertThat(response.getBody().get("code")).isEqualTo(ErrorCode.INVALID_CHARACTERS.code());
		// The request ID in the body matches the response header, so a reported error can be found in the logs.
		assertThat(response.getBody().get("requestId")).isEqualTo(response.getHeaders().getFirst(Headers.REQUEST_ID));
		assertThat(fieldErrors(response)).isEmpty();
		// The rejected input is never echoed back.
		assertThat(response.getBody().toString()).doesNotContain("script", "onerror");
	}

	@Test
	void leadingAndTrailingWhitespaceIsTrimmed() {
		ResponseEntity<Map> response = postRaw(
				"{\"loanAmount\":\"  10000.50 \",\"loanTermInMonths\":\" 12 \",\"riskBand\":\"  LOW  \"}");

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).containsKey("quoteId");
	}

	@Test
	void malformedNumberIsReportedAgainstTheField() {
		ResponseEntity<Map> response = postRaw("{\"loanAmount\":\"1.2.3\",\"loanTermInMonths\":12,\"riskBand\":\"LOW\"}");

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat(fieldErrors(response)).containsEntry("loanAmount", ValidationMessages.MUST_BE_NUMBER);
	}

	@Test
	void decimalTermIsRejectedRatherThanTruncated() {
		ResponseEntity<Map> response = postRaw("{\"loanAmount\":10000,\"loanTermInMonths\":12.5,\"riskBand\":\"LOW\"}");

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat(fieldErrors(response)).containsEntry("loanTermInMonths", ValidationMessages.MUST_BE_WHOLE_NUMBER);
	}

	@Test
	void wrongMethodReturnsMethodNotAllowed() {
		ResponseEntity<Map> response = client.get().uri(Paths.QUOTES).retrieve().toEntity(Map.class);

		assertThat(response.getStatusCode().value()).isEqualTo(405);
		assertThat(response.getBody()).containsKey("message");
	}

	@Test
	void wrongContentTypeReturnsUnsupportedMediaType() {
		ResponseEntity<Map> response = client.post()
			.uri(Paths.QUOTES)
			.contentType(MediaType.TEXT_PLAIN)
			.body("hello")
			.retrieve()
			.toEntity(Map.class);

		assertThat(response.getStatusCode().value()).isEqualTo(415);
	}

	@Test
	void unknownPathReturnsNotFound() {
		ResponseEntity<Map> response = client.get().uri("/api/does-not-exist").retrieve().toEntity(Map.class);

		assertThat(response.getStatusCode().value()).isEqualTo(404);
		assertThat(response.getBody().get("message")).isEqualTo(ErrorMessages.NOT_FOUND);
		assertThat(response.getBody().get("code")).isEqualTo(ErrorCode.NOT_FOUND.code());
	}

	@Test
	void securityHeadersAreOnEveryResponse() {
		ResponseEntity<Map> response = client.get().uri("/api/does-not-exist").retrieve().toEntity(Map.class);

		assertThat(response.getHeaders().getFirst("Content-Security-Policy"))
			.isEqualTo(SecurityHeaders.CONTENT_SECURITY_POLICY);
		assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
		assertThat(response.getHeaders().getFirst("X-Frame-Options")).isEqualTo("DENY");
	}

	@Test
	void healthEndpointReportsUp() {
		ResponseEntity<Map> response = client.get().uri("/actuator/health").retrieve().toEntity(Map.class);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).containsEntry("status", "UP");
	}

	private ResponseEntity<Map> postRaw(String json) {
		return client.post()
			.uri(Paths.QUOTES)
			.contentType(MediaType.APPLICATION_JSON)
			.body(json)
			.retrieve()
			.toEntity(Map.class);
	}

	private static Map<String, String> fieldErrors(ResponseEntity<Map> response) {
		return (Map<String, String>) response.getBody().get("fieldErrors");
	}
}
