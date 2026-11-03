package com.example.commissionquote.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Money calculations. Always uses {@link BigDecimal} so there are no floating-point rounding errors. */
public final class MoneyUtils {

	/** Money amounts are rounded to cents. */
	public static final int MONEY_SCALE = 2;

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

	private MoneyUtils() {
	}

	/**
	 * Returns {@code percent}% of {@code amount}, rounded half-up to 2 decimal places.
	 * Example: percentOf(12345.67, 1.75) = 216.05.
	 */
	public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
		return amount.multiply(percent).divide(ONE_HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
	}
}
