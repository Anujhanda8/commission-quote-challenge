package com.example.commissionquote.common.json;

import java.math.BigDecimal;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import com.example.commissionquote.common.util.InputSanitizer;

/**
 * Reads a decimal field (e.g. loanAmount) from its raw JSON text, after sanitizing it.
 * Accepts a JSON number or a string; "1.2.3" passes the character check but is still reported as "must be a number".
 */
public class SanitizedDecimalDeserializer extends ValueDeserializer<BigDecimal> {

	@Override
	public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) {
		String value = InputSanitizer.sanitizeNumber(p.getString());
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException ex) {
			return (BigDecimal) ctxt.handleWeirdStringValue(BigDecimal.class, value, "not a valid number");
		}
	}
}
