package com.example.commissionquote.vendor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.commissionquote.common.AppConstants.Config;
import com.example.commissionquote.common.util.MoneyUtils;

/**
 * The mock vendor's commission rules, configured under mock-vendor.commission:
 * rate = the configured rate for the risk band (the brief doesn't define a formula; these are mock values).
 * The loan term doesn't affect the rate.
 * totalCommission = loanAmount x rate / 100, rounded half-up to 2 decimal places.
 */
@Component
@ConditionalOnProperty(name = Config.MOCK_VENDOR_ENABLED, havingValue = "true")
public class CommissionCalculator {

	private final MockVendorProperties.Commission rules;

	public CommissionCalculator(MockVendorProperties properties) {
		this.rules = properties.commission();
		// Fail at startup rather than on the first quote for a band that has no rate.
		List<VendorRiskBand> missing = Arrays.stream(VendorRiskBand.values())
			.filter(band -> !rules.rates().containsKey(band))
			.toList();
		if (!missing.isEmpty()) {
			throw new IllegalStateException("mock-vendor.commission.rates is missing rates for " + missing);
		}
	}

	/** Returns the commission rate as a percentage (e.g. 1.75). */
	public BigDecimal rateFor(VendorRiskBand riskBand) {
		return rules.rates().get(riskBand);
	}

	public BigDecimal totalCommission(BigDecimal loanAmount, BigDecimal ratePercent) {
		return MoneyUtils.percentOf(loanAmount, ratePercent);
	}
}
