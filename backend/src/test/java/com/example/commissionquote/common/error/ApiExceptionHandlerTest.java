package com.example.commissionquote.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.example.commissionquote.common.AppConstants.ErrorMessages;

class ApiExceptionHandlerTest {

	private final ApiExceptionHandler handler = new ApiExceptionHandler();

	@Test
	void unexpectedErrorsReturnGenericMessageWithoutInternalDetails() {
		ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new IllegalStateException("db password=secret"));

		assertThat(response.getStatusCode().value()).isEqualTo(500);
		assertThat(response.getBody().message()).isEqualTo(ErrorMessages.UNEXPECTED);
		assertThat(response.getBody().code()).isEqualTo(ErrorCode.INTERNAL_ERROR.code());
		assertThat(response.getBody().message()).doesNotContain("secret");
	}
}
