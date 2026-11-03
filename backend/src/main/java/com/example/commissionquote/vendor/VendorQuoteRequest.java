package com.example.commissionquote.vendor;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import tools.jackson.databind.annotation.JsonDeserialize;

import com.example.commissionquote.common.json.SanitizedDecimalDeserializer;
import com.example.commissionquote.common.json.SanitizedEnumDeserializer;
import com.example.commissionquote.common.json.SanitizedIntegerDeserializer;

/**
 * Request body of the vendor contract. The vendor only does basic checks; business limits are enforced by our app.
 * Input is sanitized the same way as our own API's (see InputSanitizer).
 */
public record VendorQuoteRequest(
		@NotNull @Positive @JsonDeserialize(using = SanitizedDecimalDeserializer.class) BigDecimal loanAmount,
		@NotNull @Positive @JsonDeserialize(using = SanitizedIntegerDeserializer.class) Integer loanTermInMonths,
		@NotNull @JsonDeserialize(using = SanitizedEnumDeserializer.class) VendorRiskBand riskBand) {
}
