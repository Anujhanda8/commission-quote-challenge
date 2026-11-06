package com.example.commissionquote.vendor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.example.commissionquote.common.AppConstants.Config;

/**
 * Settings for the mock vendor (the "mock-vendor" section of application.yml). Validated at startup.
 *
 * @param apiKey the only api-key the mock accepts
 * @param failureRate share of requests that fail at random, from 0 (never) to 1 (always)
 * @param timeoutDelay how long a timeout-scenario request waits before answering
 * @param scenarios loan amounts that force a specific outcome
 * @param commission the commission rules
 */
/* Only bound (and validated) when the mock is on, so a real deployment doesn't need a mock API key. */
@ConditionalOnProperty(name = Config.MOCK_VENDOR_ENABLED, havingValue = "true")
@Validated
@ConfigurationProperties(prefix = "mock-vendor")
public record MockVendorProperties(
		@NotBlank String apiKey,
		@DecimalMin("0.0") @DecimalMax("1.0") double failureRate,
		@NotNull Duration timeoutDelay,
		@Valid @NotNull Scenarios scenarios,
		@Valid @NotNull Commission commission) {

	/**
	 * @param errorAmount loan amount that always gets HTTP 500
	 * @param timeoutAmount loan amount that always gets a delayed response
	 */
	public record Scenarios(@NotNull BigDecimal errorAmount, @NotNull BigDecimal timeoutAmount) {
	}

	/**
	 * @param rates commission rate (percentage) per risk band; every band must be present
	 */
	public record Commission(@NotEmpty Map<VendorRiskBand, BigDecimal> rates) {
	}
}
