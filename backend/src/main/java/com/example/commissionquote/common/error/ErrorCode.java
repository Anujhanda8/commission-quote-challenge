package com.example.commissionquote.common.error;

import org.springframework.http.HttpStatus;

import com.example.commissionquote.common.AppConstants.ErrorMessages;

/**
 * Every error the backend can return, each with a fixed ID. The ID is sent in the error response (so the frontend
 * can react to specific errors) and written on the log line (so a reported error can be found in the logs).
 *
 * Ranges: CQ-1xxx invalid requests, CQ-2xxx vendor problems, CQ-3xxx mock vendor, CQ-9xxx unexpected errors.
 * The frontend lists the IDs it handles in frontend/src/constants.js (ERROR_CODES).
 */
public enum ErrorCode {

	VALIDATION_FAILED("CQ-1001", HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_LOAN_DETAILS),
	INVALID_CHARACTERS("CQ-1002", HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_CHARACTERS),
	UNREADABLE_BODY("CQ-1003", HttpStatus.BAD_REQUEST, ErrorMessages.UNREADABLE_BODY),
	NOT_FOUND("CQ-1004", HttpStatus.NOT_FOUND, ErrorMessages.NOT_FOUND),
	METHOD_NOT_ALLOWED("CQ-1005", HttpStatus.METHOD_NOT_ALLOWED, ErrorMessages.METHOD_NOT_ALLOWED),
	UNSUPPORTED_MEDIA_TYPE("CQ-1006", HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorMessages.UNSUPPORTED_MEDIA_TYPE),

	VENDOR_TIMEOUT("CQ-2001", HttpStatus.GATEWAY_TIMEOUT, ErrorMessages.VENDOR_TIMEOUT),
	VENDOR_ERROR("CQ-2002", HttpStatus.BAD_GATEWAY, ErrorMessages.VENDOR_ERROR),
	VENDOR_EMPTY_RESPONSE("CQ-2003", HttpStatus.BAD_GATEWAY, ErrorMessages.VENDOR_EMPTY_RESPONSE),
	/** The vendor rejected our API key: a configuration problem on our side. The user sees the generic vendor error. */
	VENDOR_AUTH_FAILED("CQ-2004", HttpStatus.BAD_GATEWAY, ErrorMessages.VENDOR_ERROR),

	MOCK_VENDOR_INVALID_API_KEY("CQ-3001", HttpStatus.UNAUTHORIZED, ErrorMessages.INVALID_API_KEY),
	MOCK_VENDOR_SIMULATED_FAILURE("CQ-3002", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.SIMULATED_VENDOR_FAILURE),

	INTERNAL_ERROR("CQ-9001", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.UNEXPECTED);

	private final String code;

	private final HttpStatus status;

	private final String message;

	ErrorCode(String code, HttpStatus status, String message) {
		this.code = code;
		this.status = status;
		this.message = message;
	}

	/** The ID sent to clients and written to the logs, e.g. "CQ-2001". */
	public String code() {
		return code;
	}

	public HttpStatus status() {
		return status;
	}

	/** User-friendly message; never contains internal details. */
	public String message() {
		return message;
	}
}
