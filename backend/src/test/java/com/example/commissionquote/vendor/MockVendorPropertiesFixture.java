package com.example.commissionquote.vendor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/** Builds {@link MockVendorProperties} for unit tests, with the same values as application.yml. */
public final class MockVendorPropertiesFixture {

	public static final String API_KEY = "test-key";

	public static final BigDecimal ERROR_AMOUNT = new BigDecimal("1001");

	public static final BigDecimal TIMEOUT_AMOUNT = new BigDecimal("1002");

	private MockVendorPropertiesFixture() {
	}

	public static MockVendorProperties properties(double failureRate, Duration timeoutDelay) {
		return new MockVendorProperties(API_KEY, failureRate, timeoutDelay,
				new MockVendorProperties.Scenarios(ERROR_AMOUNT, TIMEOUT_AMOUNT), commission());
	}

	public static MockVendorProperties.Commission commission() {
		return new MockVendorProperties.Commission(
				Map.of(VendorRiskBand.LOW, new BigDecimal("1.00"), VendorRiskBand.MEDIUM, new BigDecimal("1.50"),
						VendorRiskBand.HIGH, new BigDecimal("2.00")));
	}
}
