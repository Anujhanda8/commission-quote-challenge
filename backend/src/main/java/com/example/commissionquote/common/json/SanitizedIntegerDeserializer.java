package com.example.commissionquote.common.json;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import com.example.commissionquote.common.util.InputSanitizer;

/**
 * Reads a whole-number field (e.g. loanTermInMonths) from its raw JSON text, after sanitizing it.
 * Values that pass the character check but aren't whole numbers ("12.5") or don't fit an int are reported as
 * "must be a whole number", never truncated.
 */
public class SanitizedIntegerDeserializer extends ValueDeserializer<Integer> {

	@Override
	public Integer deserialize(JsonParser p, DeserializationContext ctxt) {
		String value = InputSanitizer.sanitizeNumber(p.getString());
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			return (Integer) ctxt.handleWeirdStringValue(Integer.class, value, "not a whole number");
		}
	}
}
