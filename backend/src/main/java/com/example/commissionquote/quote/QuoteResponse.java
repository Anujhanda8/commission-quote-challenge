package com.example.commissionquote.quote;

import java.math.BigDecimal;

/**
 * The commission quote returned to the user.
 *
 * @param quoteId the vendor's ID for this quote
 * @param commissionRate a percentage, e.g. 1.75 means 1.75%
 * @param totalCommission the commission amount, rounded to 2 decimal places
 */
public record QuoteResponse(String quoteId, BigDecimal commissionRate, BigDecimal totalCommission) {
}
