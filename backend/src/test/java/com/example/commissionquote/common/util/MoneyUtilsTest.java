package com.example.commissionquote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyUtilsTest {

	@ParameterizedTest
	@CsvSource({
			"100000,   1.75, 1750.00",
			"12345.67, 1.75, 216.05", // 216.049225 rounds up
			"1000,     1.00, 10.00",
			"1000.01,  1.25, 12.50", // 12.500125 rounds down
			"0.02,     25,   0.01" // 0.005 rounds half-up
	})
	void calculatesPercentageRoundedToCents(String amount, String percent, String expected) {
		assertThat(MoneyUtils.percentOf(new BigDecimal(amount), new BigDecimal(percent)))
			.isEqualTo(new BigDecimal(expected));
	}
}
