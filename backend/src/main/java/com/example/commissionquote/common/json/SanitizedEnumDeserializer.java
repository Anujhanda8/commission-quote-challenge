package com.example.commissionquote.common.json;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import com.example.commissionquote.common.util.InputSanitizer;

/**
 * Reads an enum field (e.g. riskBand) after sanitizing it: only the exact enum names are accepted.
 * Works for any enum: Jackson calls createContextual with the annotated property, which tells us the enum type.
 */
public class SanitizedEnumDeserializer<E extends Enum<E>> extends ValueDeserializer<E> {

	private final Class<E> type;

	/** Used by Jackson to create the initial instance; the typed one comes from createContextual. */
	public SanitizedEnumDeserializer() {
		this(null);
	}

	private SanitizedEnumDeserializer(Class<E> type) {
		this.type = type;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
		return new SanitizedEnumDeserializer(property.getType().getRawClass());
	}

	@Override
	public E deserialize(JsonParser p, DeserializationContext ctxt) {
		return InputSanitizer.sanitizeEnum(p.getString(), type);
	}
}
