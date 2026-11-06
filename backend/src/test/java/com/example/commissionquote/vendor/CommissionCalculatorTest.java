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
			"LOW,    1.00",
			"MEDIUM, 1.50",
			"HIGH,   2.00" })
	void rateDependsOnRiskBandOnly(VendorRiskBand riskBand, String expectedRate) {
		assertThat(calculator.rateFor(riskBand)).isEqualByComparingTo(expectedRate);
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
				Map.of(VendorRiskBand.LOW, BigDecimal.ONE));
		MockVendorProperties properties = new MockVendorProperties("key", 0.0, Duration.ZERO,
				new MockVendorProperties.Scenarios(BigDecimal.ONE, BigDecimal.TEN), incomplete);

		assertThatThrownBy(() -> new CommissionCalculator(properties))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("MEDIUM")
			.hasMessageContaining("HIGH");
	}
}
