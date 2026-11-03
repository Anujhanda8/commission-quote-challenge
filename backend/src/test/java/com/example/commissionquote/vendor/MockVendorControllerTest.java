package com.example.commissionquote.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class MockVendorControllerTest {

	@Test
	void returnsCalculatedQuoteWhenNotFailing() throws InterruptedException {
		ResponseEntity<?> response = controller(0.0, Duration.ZERO)
			.createQuote(request("100000", 72, VendorRiskBand.MEDIUM));

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		VendorQuoteResponse quote = (VendorQuoteResponse) response.getBody();
		assertThat(quote.quoteId()).isNotBlank();
		assertThat(quote.commissionRate()).isEqualByComparingTo("1.75");
		assertThat(quote.totalCommission()).isEqualByComparingTo("1750.00");
	}

	@Test
	void generatesUniqueQuoteIds() throws InterruptedException {
		MockVendorController controller = controller(0.0, Duration.ZERO);
		VendorQuoteRequest request = request("10000", 12, VendorRiskBand.LOW);

		Object first = controller.createQuote(request).getBody();
		Object second = controller.createQuote(request).getBody();

		assertThat(((VendorQuoteResponse) first).quoteId()).isNotEqualTo(((VendorQuoteResponse) second).quoteId());
	}

	@Test
	void alwaysFailsWhenFailureRateIsOne() throws InterruptedException {
		ResponseEntity<?> response = controller(1.0, Duration.ZERO).createQuote(request("10000", 12, VendorRiskBand.LOW));

		assertThat(response.getStatusCode().value()).isEqualTo(500);
	}

	@Test
	void errorScenarioAmountFailsEvenWithNoRandomFailures() throws InterruptedException {
		// Different scale on purpose: 1001.00 must match 1001.
		ResponseEntity<?> response = controller(0.0, Duration.ZERO)
			.createQuote(request(MockVendorPropertiesFixture.ERROR_AMOUNT.setScale(2).toPlainString(), 12,
					VendorRiskBand.LOW));

		assertThat(response.getStatusCode().value()).isEqualTo(500);
	}

	@Test
	void timeoutScenarioAmountDelaysThenResponds() throws InterruptedException {
		MockVendorController controller = controller(0.0, Duration.ofMillis(300));

		long start = System.currentTimeMillis();
		ResponseEntity<?> response = controller
			.createQuote(request(MockVendorPropertiesFixture.TIMEOUT_AMOUNT.toPlainString(), 12, VendorRiskBand.LOW));

		assertThat(System.currentTimeMillis() - start).isGreaterThanOrEqualTo(300);
		assertThat(response.getStatusCode().value()).isEqualTo(200);
	}

	private static MockVendorController controller(double failureRate, Duration timeoutDelay) {
		MockVendorProperties properties = MockVendorPropertiesFixture.properties(failureRate, timeoutDelay);
		return new MockVendorController(new CommissionCalculator(properties), properties);
	}

	private static VendorQuoteRequest request(String amount, int term, VendorRiskBand riskBand) {
		return new VendorQuoteRequest(new BigDecimal(amount), term, riskBand);
	}
}
