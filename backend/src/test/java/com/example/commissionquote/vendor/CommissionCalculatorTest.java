package com.example.commissionquote.vendor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommissionCalculatorTest {

	private final CommissionCalculator calculator = new CommissionCalculator(
			MockVendorPropertiesFixture.properties(0.0, Duration.ZERO));

	@ParameterizedTest
	@CsvSource({
			"LOW,    12, 1.00",
			"MEDIUM, 12, 1.50",
			"HIGH,   12, 2.00",
			"LOW,    60, 1.00", // threshold itself: no adjustment
			"LOW,    61, 1.25",
			"MEDIUM, 360, 1.75",
			"HIGH,   61, 2.25" })
	void rateDependsOnRiskBandAndTerm(VendorRiskBand riskBand, int term, String expectedRate) {
		assertThat(calculator.rateFor(riskBand, term)).isEqualByComparingTo(expectedRate);
	}

	@Test
	void totalCommissionIsRoundedToTwoDecimalPlaces() {
		// 12345.67 * 1.75% = 216.049225
		assertThat(calculator.totalCommission(new BigDecimal("12345.67"), new BigDecimal("1.75")))
			.isEqualTo(new BigDecimal("216.05"));
	}

	@Test
	void failsAtStartupWhenARiskBandHasNoRate() {
		MockVendorProperties.Commission incomplete = new MockVendorProperties.Commission(
				Map.of(VendorRiskBand.LOW, BigDecimal.ONE), 60, new BigDecimal("0.25"));
		MockVendorProperties properties = new MockVendorProperties("key", 0.0, Duration.ZERO,
				new MockVendorProperties.Scenarios(BigDecimal.ONE, BigDecimal.TEN), incomplete);

		assertThatThrownBy(() -> new CommissionCalculator(properties))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("MEDIUM")
			.hasMessageContaining("HIGH");
	}
}
