package com.example.commissionquote.quote;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuoteRequestValidationTest {

	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@ParameterizedTest
	@ValueSource(strings = { "1000", "1000.00", "250000.55", "5000000" })
	void acceptsAmountsWithinRange(String amount) {
		assertThat(invalidFields(new QuoteRequest(new BigDecimal(amount), 12, RiskBand.LOW))).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = { "999.99", "5000000.01", "-1", "0", "1000.123" })
	void rejectsInvalidAmounts(String amount) {
		assertThat(invalidFields(new QuoteRequest(new BigDecimal(amount), 12, RiskBand.LOW)))
			.containsExactly("loanAmount");
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 60, 360 })
	void acceptsTermsWithinRange(int term) {
		assertThat(invalidFields(new QuoteRequest(new BigDecimal("10000"), term, RiskBand.HIGH))).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, -5, 361 })
	void rejectsInvalidTerms(int term) {
		assertThat(invalidFields(new QuoteRequest(new BigDecimal("10000"), term, RiskBand.HIGH)))
			.containsExactly("loanTermInMonths");
	}

	@Test
	void requiresAllFields() {
		assertThat(invalidFields(new QuoteRequest(null, null, null)))
			.containsExactlyInAnyOrder("loanAmount", "loanTermInMonths", "riskBand");
	}

	private static Set<String> invalidFields(QuoteRequest request) {
		return validator.validate(request)
			.stream()
			.map(ConstraintViolation::getPropertyPath)
			.map(Object::toString)
			.collect(Collectors.toSet());
	}
}
