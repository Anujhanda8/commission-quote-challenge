package com.example.commissionquote.quote;

import org.springframework.stereotype.Service;

import com.example.commissionquote.common.logging.SecureLogger;
import com.example.commissionquote.quote.client.CommissionVendorClient;

/**
 * Business logic for generating quotes. Today it only delegates to the vendor; this is where persisting quotes,
 * auditing or extra business rules would go.
 */
@Service
public class QuoteService {

	private static final SecureLogger log = SecureLogger.getLogger(QuoteService.class);

	private final CommissionVendorClient vendorClient;

	public QuoteService(CommissionVendorClient vendorClient) {
		this.vendorClient = vendorClient;
	}

	public QuoteResponse generateQuote(QuoteRequest request) {
		// Loan amounts are deliberately not logged.
		log.info("Requesting commission quote: term={} months, riskBand={}", request.loanTermInMonths(),
				request.riskBand());
		QuoteResponse quote = vendorClient.requestQuote(request);
		log.info("Commission quote generated: quoteId={}", quote.quoteId());
		return quote;
	}
}
