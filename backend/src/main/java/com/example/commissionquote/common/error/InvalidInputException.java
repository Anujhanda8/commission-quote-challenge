package com.example.commissionquote.common.error;

import com.example.commissionquote.common.AppConstants.ErrorMessages;

/**
 * Input contained characters or values outside the allowed set (see InputSanitizer).
 * Mapped to a 400 with a generic message; the rejected value is never logged or echoed back.
 */
public class InvalidInputException extends RuntimeException {

	public InvalidInputException() {
		super(ErrorMessages.INVALID_CHARACTERS);
	}
}
