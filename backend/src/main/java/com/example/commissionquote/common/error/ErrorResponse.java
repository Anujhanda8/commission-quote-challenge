package com.example.commissionquote.common.error;

import java.util.Map;

import org.slf4j.MDC;

import com.example.commissionquote.common.AppConstants.Logging;

/**
 * The single error body shape returned by every endpoint.
 *
 * @param code the error ID, e.g. "CQ-2001" (see {@link ErrorCode}); the frontend can react to specific codes
 * @param message a user-friendly description of what went wrong
 * @param fieldErrors validation messages per request field; empty when the error isn't about specific fields
 * @param requestId ID of the request, also on every log line for it, so a reported error can be traced
 */
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors, String requestId) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return of(errorCode, Map.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, Map<String, String> fieldErrors) {
		return new ErrorResponse(errorCode.code(), errorCode.message(), fieldErrors,
				MDC.get(Logging.REQUEST_ID_MDC_KEY));
	}
}
