package com.example.commissionquote.quote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.example.commissionquote.quote.client.CommissionVendorClient;
import com.example.commissionquote.common.error.ErrorCode;
import com.example.commissionquote.quote.client.VendorException;

class QuoteServiceTest {

	private final CommissionVendorClient vendorClient = mock(CommissionVendorClient.class);

	private final QuoteService service = new QuoteService(vendorClient);

	private final QuoteRequest request = new QuoteRequest(new BigDecimal("10000"), 12, RiskBand.LOW);

	@Test
	void returnsQuoteFromVendor() {
		QuoteResponse quote = new QuoteResponse("q-1", new BigDecimal("1.00"), new BigDecimal("100.00"));
		when(vendorClient.requestQuote(request)).thenReturn(quote);

		assertThat(service.generateQuote(request)).isEqualTo(quote);
	}

	@Test
	void propagatesVendorFailures() {
		when(vendorClient.requestQuote(request))
			.thenThrow(new VendorException(ErrorCode.VENDOR_TIMEOUT, "Vendor timed out", null));

		assertThatThrownBy(() -> service.generateQuote(request)).isInstanceOf(VendorException.class);
	}
}
