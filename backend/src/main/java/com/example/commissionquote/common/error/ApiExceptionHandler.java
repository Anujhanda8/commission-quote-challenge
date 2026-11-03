package com.example.commissionquote.common.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import tools.jackson.databind.exc.MismatchedInputException;

import com.example.commissionquote.common.AppConstants.ValidationMessages;
import com.example.commissionquote.common.logging.SecureLogger;
import com.example.commissionquote.common.util.ExceptionUtils;
import com.example.commissionquote.quote.client.VendorException;

/**
 * Global error handling: every failure becomes an {@link ErrorResponse} with its {@link ErrorCode}.
 * This is also the one place that logs handled errors, each with its code. Log lines only contain our own text and
 * field names, never request values or library messages (which can include the rejected input).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final SecureLogger log = SecureLogger.getLogger(ApiExceptionHandler.class);

	/** Bean Validation failures (@Valid request bodies): 400 with one message per invalid field. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			// A field can break several rules; report the first one only.
			fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		log.info(ErrorCode.VALIDATION_FAILED, "Validation failed for fields {}", fieldErrors.keySet());
		return respond(ErrorCode.VALIDATION_FAILED, fieldErrors);
	}

	/**
	 * Body that couldn't be read.
	 * - Input rejected by InputSanitizer (e.g. "abc" or "<script>" in a number field, an unknown riskBand): a
	 *   generic "invalid characters" message, with no field details.
	 * - A field with characters that are allowed but don't form a valid value (e.g. 12.5 for loanTermInMonths):
	 *   reported against that field like a validation error.
	 * - Anything else (e.g. broken JSON): a general message.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
		if (ExceptionUtils.hasCause(ex, InvalidInputException.class)) {
			log.info(ErrorCode.INVALID_CHARACTERS, "Request rejected: input contains invalid characters");
			return respond(ErrorCode.INVALID_CHARACTERS, Map.of());
		}
		MismatchedInputException typeError = ExceptionUtils.findCause(ex, MismatchedInputException.class);
		if (typeError != null && !typeError.getPath().isEmpty()) {
			String field = typeError.getPath().get(typeError.getPath().size() - 1).getPropertyName();
			if (field != null) {
				log.info(ErrorCode.VALIDATION_FAILED, "Invalid value for field {}", field);
				return respond(ErrorCode.VALIDATION_FAILED, Map.of(field, typeMessage(typeError.getTargetType())));
			}
		}
		log.info(ErrorCode.UNREADABLE_BODY, "Unreadable request body");
		return respond(ErrorCode.UNREADABLE_BODY, Map.of());
	}

	/** Vendor failures map to gateway errors (502 / 504); the code comes from the vendor client. */
	@ExceptionHandler(VendorException.class)
	public ResponseEntity<ErrorResponse> handleVendor(VendorException ex) {
		ErrorCode code = ex.getErrorCode();
		if (code == ErrorCode.VENDOR_AUTH_FAILED) {
			// Our configuration is wrong, not the user's input: make it stand out.
			log.error(code, null, "{}. Check the VENDOR_API_KEY setting.", ex.getMessage());
		}
		else {
			log.warn(code, "{}", ex.getMessage());
		}
		return respond(code, Map.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex) {
		log.info(ErrorCode.NOT_FOUND, "No resource found");
		return respond(ErrorCode.NOT_FOUND, Map.of());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
		log.info(ErrorCode.METHOD_NOT_ALLOWED, "HTTP method {} not supported", ex.getMethod());
		return respond(ErrorCode.METHOD_NOT_ALLOWED, Map.of());
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
		log.info(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type");
		return respond(ErrorCode.UNSUPPORTED_MEDIA_TYPE, Map.of());
	}

	/** Safety net for anything not handled above: log the full error, return a generic message. */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		log.error(ErrorCode.INTERNAL_ERROR, ex, "Unexpected error");
		return respond(ErrorCode.INTERNAL_ERROR, Map.of());
	}

	/** Describes what type the field expects, in user-friendly terms. */
	private static String typeMessage(Class<?> targetType) {
		if (targetType == Integer.class || targetType == int.class) {
			return ValidationMessages.MUST_BE_WHOLE_NUMBER;
		}
		if (targetType != null && Number.class.isAssignableFrom(targetType)) {
			return ValidationMessages.MUST_BE_NUMBER;
		}
		return ValidationMessages.INVALID_VALUE;
	}

	private static ResponseEntity<ErrorResponse> respond(ErrorCode code, Map<String, String> fieldErrors) {
		return ResponseEntity.status(code.status()).body(ErrorResponse.of(code, fieldErrors));
	}
}
