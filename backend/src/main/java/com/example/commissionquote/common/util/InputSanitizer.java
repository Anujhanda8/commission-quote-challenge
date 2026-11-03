package com.example.commissionquote.common.util;

import java.util.regex.Pattern;

import com.example.commissionquote.common.AppConstants.InputRules;
import com.example.commissionquote.common.error.InvalidInputException;

/**
 * Sanitizes raw input values before they are converted and processed.
 *
 * Every value is trimmed, then checked against an allow-list: number fields may only contain digits and '.', and
 * enum fields (the risk band dropdown) may only be one of the enum's names. Anything else (script tags, quotes,
 * signs, exponents, control characters...) is rejected with an {@link InvalidInputException} instead of being
 * cleaned up, so altered input is never processed silently.
 */
public final class InputSanitizer {

	private static final Pattern NUMBER_CHARS = Pattern.compile(InputRules.NUMBER_CHARS_REGEX);

	private InputSanitizer() {
	}

	/** Removes leading and trailing whitespace (including Unicode spaces). */
	public static String trim(String raw) {
		return raw == null ? null : raw.strip();
	}

	/** Returns the trimmed value if it only contains digits and '.'; throws otherwise. */
	public static String sanitizeNumber(String raw) {
		String value = trim(raw);
		if (value == null || !NUMBER_CHARS.matcher(value).matches()) {
			throw new InvalidInputException();
		}
		return value;
	}

	/** Returns the enum constant whose name exactly matches the trimmed value; throws for anything else. */
	public static <E extends Enum<E>> E sanitizeEnum(String raw, Class<E> type) {
		String value = trim(raw);
		for (E constant : type.getEnumConstants()) {
			if (constant.name().equals(value)) {
				return constant;
			}
		}
		throw new InvalidInputException();
	}
}
