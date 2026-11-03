package com.example.commissionquote.vendor;

import java.math.BigDecimal;

/** Response body of the vendor contract. commissionRate is a percentage. */
public record VendorQuoteResponse(String quoteId, BigDecimal commissionRate, BigDecimal totalCommission) {
}
