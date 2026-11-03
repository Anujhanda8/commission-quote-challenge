package com.example.commissionquote.vendor;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.validation.Valid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.commissionquote.common.AppConstants.Config;
import com.example.commissionquote.common.AppConstants.Paths;
import com.example.commissionquote.common.error.ErrorCode;
import com.example.commissionquote.common.error.ErrorResponse;
import com.example.commissionquote.common.logging.SecureLogger;

/**
 * Mock of the third-party Commission Quote API, used until the real vendor is available.
 * Authentication is handled by {@link VendorApiKeyFilter}.
 *
 * To mimic real network conditions it fails at random (mock-vendor.failure-rate), and specific loan amounts
 * (mock-vendor.scenarios) force an error or a slow response, so each case can be tested on demand.
 */
@RestController
@ConditionalOnProperty(name = Config.MOCK_VENDOR_ENABLED, havingValue = "true")
@RequestMapping(Paths.MOCK_VENDOR_BASE)
public class MockVendorController {

	private static final SecureLogger log = SecureLogger.getLogger(MockVendorController.class);

	private final CommissionCalculator calculator;

	private final MockVendorProperties properties;

	public MockVendorController(CommissionCalculator calculator, MockVendorProperties properties) {
		this.calculator = calculator;
		this.properties = properties;
	}

	@PostMapping(Paths.MOCK_VENDOR_QUOTES)
	public ResponseEntity<?> createQuote(@Valid @RequestBody VendorQuoteRequest request) throws InterruptedException {
		BigDecimal amount = request.loanAmount();

		// compareTo, not equals: 1001 and 1001.00 must be treated as the same amount.
		if (amount.compareTo(properties.scenarios().timeoutAmount()) == 0) {
			log.info("Mock vendor: simulating slow response ({})", properties.timeoutDelay());
			Thread.sleep(properties.timeoutDelay().toMillis());
		}
		if (amount.compareTo(properties.scenarios().errorAmount()) == 0) {
			log.info(ErrorCode.MOCK_VENDOR_SIMULATED_FAILURE, "Mock vendor: simulating error scenario");
			return simulatedFailure();
		}
		if (ThreadLocalRandom.current().nextDouble() < properties.failureRate()) {
			log.info(ErrorCode.MOCK_VENDOR_SIMULATED_FAILURE, "Mock vendor: simulating random failure");
			return simulatedFailure();
		}

		BigDecimal rate = calculator.rateFor(request.riskBand(), request.loanTermInMonths());
		BigDecimal total = calculator.totalCommission(amount, rate);
		return ResponseEntity.ok(new VendorQuoteResponse(UUID.randomUUID().toString(), rate, total));
	}

	private static ResponseEntity<ErrorResponse> simulatedFailure() {
		ErrorCode code = ErrorCode.MOCK_VENDOR_SIMULATED_FAILURE;
		return ResponseEntity.status(code.status()).body(ErrorResponse.of(code));
	}
}
