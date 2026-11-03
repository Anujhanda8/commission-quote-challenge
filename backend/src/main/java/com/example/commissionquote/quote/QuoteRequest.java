package com.example.commissionquote.quote;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import tools.jackson.databind.annotation.JsonDeserialize;

import com.example.commissionquote.common.AppConstants.LoanLimits;
import com.example.commissionquote.common.AppConstants.ValidationMessages;
import com.example.commissionquote.common.json.SanitizedDecimalDeserializer;
import com.example.commissionquote.common.json.SanitizedEnumDeserializer;
import com.example.commissionquote.common.json.SanitizedIntegerDeserializer;

/**
 * Loan details submitted by the user. Validated before anything is sent to the vendor.
 * Money uses {@link BigDecimal} to avoid floating-point rounding errors.
 * Each field is trimmed and sanitized while it is read (see InputSanitizer), before Bean Validation runs.
 */
public record QuoteRequest(
		@NotNull(message = ValidationMessages.REQUIRED)
		@DecimalMin(value = LoanLimits.MIN_AMOUNT, message = ValidationMessages.MIN_VALUE)
		@DecimalMax(value = LoanLimits.MAX_AMOUNT, message = ValidationMessages.MAX_VALUE)
		@Digits(integer = LoanLimits.AMOUNT_INTEGER_DIGITS, fraction = LoanLimits.AMOUNT_FRACTION_DIGITS,
				message = ValidationMessages.MAX_DECIMALS)
		@JsonDeserialize(using = SanitizedDecimalDeserializer.class)
		BigDecimal loanAmount,

		@NotNull(message = ValidationMessages.REQUIRED)
		@Min(value = LoanLimits.MIN_TERM_MONTHS, message = ValidationMessages.MIN_VALUE)
		@Max(value = LoanLimits.MAX_TERM_MONTHS, message = ValidationMessages.MAX_VALUE)
		@JsonDeserialize(using = SanitizedIntegerDeserializer.class)
		Integer loanTermInMonths,

		@NotNull(message = ValidationMessages.REQUIRED)
		@JsonDeserialize(using = SanitizedEnumDeserializer.class)
		RiskBand riskBand) {
}
