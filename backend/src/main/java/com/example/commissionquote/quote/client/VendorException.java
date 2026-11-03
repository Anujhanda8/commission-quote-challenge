package com.example.commissionquote.quote.client;

import com.example.commissionquote.common.error.ErrorCode;

/**
 * Any failure while calling the vendor. The {@link ErrorCode} decides the HTTP status and message the user gets;
 * the exception message holds the technical detail, which is only logged.
 */
public class VendorException extends RuntimeException {

	private final ErrorCode errorCode;

	public VendorException(ErrorCode errorCode, String detail, Throwable cause) {
		super(detail, cause);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
