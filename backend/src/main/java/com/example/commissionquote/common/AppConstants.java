package com.example.commissionquote.common;

/**
 * Fixed values used across the application: API paths, HTTP header names, validation limits and messages.
 *
 * Only values that are part of the code's contract live here. Anything that may differ between environments
 * (URLs, keys, timeouts, mock behaviour, commission rates) is configured in application.yml instead.
 */
public final class AppConstants {

	private AppConstants() {
	}

	/** URL paths exposed by this application. */
	public static final class Paths {

		private Paths() {
		}

		public static final String API_PREFIX = "/api/";

		public static final String QUOTES = "/api/quotes";

		/** Base path of the mock vendor API hosted in this app. */
		public static final String MOCK_VENDOR_BASE = "/vendor";

		public static final String MOCK_VENDOR_PREFIX = MOCK_VENDOR_BASE + "/";

		/** Quote endpoint of the mock vendor, as defined by the agreed vendor contract. */
		public static final String MOCK_VENDOR_QUOTES = "/commission-quotes";
	}

	/** HTTP header names. */
	public static final class Headers {

		private Headers() {
		}

		/** Authentication header required by the vendor contract. */
		public static final String API_KEY = "api-key";

		/** Correlates log lines for one request, across our app and the vendor. */
		public static final String REQUEST_ID = "X-Request-Id";
	}

	/** Logging context keys and rules. */
	public static final class Logging {

		private Logging() {
		}

		/** Key under which the request ID is stored in the logging context (MDC); used by the log pattern. */
		public static final String REQUEST_ID_MDC_KEY = "requestId";

		/** Key under which the error code (e.g. CQ-2001) of a logged error is stored in the MDC; used by the log pattern. */
		public static final String ERROR_CODE_MDC_KEY = "errorCode";

		/** Caller-supplied request IDs must match this, so they can't inject arbitrary text into our logs. */
		public static final String SAFE_REQUEST_ID_REGEX = "[A-Za-z0-9-]{1,64}";
	}

	/**
	 * Limits for loan details. These must be compile-time constants because they are used in validation annotations.
	 * The frontend applies the same limits in frontend/src/constants.js.
	 */
	public static final class LoanLimits {

		private LoanLimits() {
		}

		public static final String MIN_AMOUNT = "1000";

		public static final String MAX_AMOUNT = "5000000";

		/** Maximum digits before the decimal point (5,000,000 has 7). */
		public static final int AMOUNT_INTEGER_DIGITS = 7;

		/** Money has at most 2 decimal places. */
		public static final int AMOUNT_FRACTION_DIGITS = 2;

		public static final int MIN_TERM_MONTHS = 1;

		public static final int MAX_TERM_MONTHS = 360;
	}

	/** Configuration property names that code refers to (e.g. in @ConditionalOnProperty). */
	public static final class Config {

		private Config() {
		}

		/** Turns the mock vendor on. Off by default, so a real deployment never exposes it by accident. */
		public static final String MOCK_VENDOR_ENABLED = "mock-vendor.enabled";
	}

	/** Security headers added to every response by SecurityHeadersFilter. */
	public static final class SecurityHeaders {

		private SecurityHeaders() {
		}

		/**
		 * Only our own scripts, styles, images and fonts may load. 'unsafe-inline' for styles is needed because MUI
		 * (Emotion) injects its styles at runtime; scripts stay strict.
		 */
		public static final String CONTENT_SECURITY_POLICY = "default-src 'self'; script-src 'self'; "
				+ "style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:; "
				+ "object-src 'none'; base-uri 'self'; frame-ancestors 'none'";

		/** Stops browsers guessing content types (e.g. running a JSON response as a script). */
		public static final String CONTENT_TYPE_OPTIONS = "nosniff";

		/** The app may not be shown inside a frame (clickjacking protection). */
		public static final String FRAME_OPTIONS = "DENY";

		/** Don't send our URLs to other sites. */
		public static final String REFERRER_POLICY = "no-referrer";
	}

	/** Allow-list used by InputSanitizer: what characters each kind of input may contain (after trimming). */
	public static final class InputRules {

		private InputRules() {
		}

		/** Number fields: digits and '.' only. No signs, exponents, spaces, commas or anything else. */
		public static final String NUMBER_CHARS_REGEX = "[0-9.]+";
	}

	/**
	 * Field-level validation messages. {value}, {fraction} etc. are filled in by Bean Validation from the
	 * annotation's attributes, so the limits are never repeated in the text.
	 */
	public static final class ValidationMessages {

		private ValidationMessages() {
		}

		public static final String REQUIRED = "is required";

		public static final String MIN_VALUE = "must be at least {value}";

		public static final String MAX_VALUE = "must be at most {value}";

		public static final String MAX_DECIMALS = "must have at most {fraction} decimal places";

		/** Used when a JSON value has the wrong type, e.g. "abc" for a number. */
		public static final String MUST_BE_NUMBER = "must be a number";

		public static final String MUST_BE_WHOLE_NUMBER = "must be a whole number";

		public static final String INVALID_VALUE = "has an invalid value";
	}

	/** Messages returned to API clients. They are shown to users, so they must never contain internal details. */
	public static final class ErrorMessages {

		private ErrorMessages() {
		}

		public static final String INVALID_LOAN_DETAILS = "Invalid loan details";

		public static final String UNREADABLE_BODY = "Request body is missing or contains invalid values";

		/** Generic on purpose: we don't say which field or character, and never echo the rejected input back. */
		public static final String INVALID_CHARACTERS = "Request contains invalid characters";

		public static final String VENDOR_TIMEOUT = "The commission vendor timed out. Please try again.";

		public static final String VENDOR_ERROR = "The commission vendor could not generate a quote. Please try again.";

		public static final String VENDOR_EMPTY_RESPONSE = "The commission API returned an empty response, so a quote "
				+ "could not be generated for the given input. Please try again.";

		public static final String NOT_FOUND = "Resource not found";

		public static final String METHOD_NOT_ALLOWED = "This HTTP method is not supported";

		public static final String UNSUPPORTED_MEDIA_TYPE = "Content type must be application/json";

		public static final String UNEXPECTED = "An unexpected error occurred. Please try again later.";

		public static final String INVALID_API_KEY = "Invalid or missing API key";

		public static final String SIMULATED_VENDOR_FAILURE = "Simulated vendor failure";
	}
}
