package com.example.commissionquote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.commissionquote.common.error.InvalidInputException;

class InputSanitizerTest {

	private enum Band {
		LOW, HIGH
	}

	@Test
	void trimsLeadingAndTrailingWhitespace() {
		assertThat(InputSanitizer.trim("  5000 \t\n")).isEqualTo("5000");
		assertThat(InputSanitizer.trim(null)).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = { "5000", "2500.50", " 12 ", "1.2.3" })
	void numberAcceptsDigitsAndDotsAfterTrimming(String raw) {
		assertThat(InputSanitizer.sanitizeNumber(raw)).isEqualTo(raw.strip());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "abc", "<script>alert(1)</script>", "-5000", "+5000", "1e5", "1,000", "12 3", "5000;",
			"\u0000", "'5000'", "   " })
	void numberRejectsAnythingElse(String raw) {
		assertThatThrownBy(() -> InputSanitizer.sanitizeNumber(raw)).isInstanceOf(InvalidInputException.class);
	}

	@Test
	void enumAcceptsExactNameAfterTrimming() {
		assertThat(InputSanitizer.sanitizeEnum(" LOW ", Band.class)).isEqualTo(Band.LOW);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "low", "EXTREME", "<b>LOW</b>", "LOW;", "L OW" })
	void enumRejectsAnythingElse(String raw) {
		assertThatThrownBy(() -> InputSanitizer.sanitizeEnum(raw, Band.class)).isInstanceOf(InvalidInputException.class);
	}
}
