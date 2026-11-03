package com.example.commissionquote.vendor;

/**
 * Risk band as defined by the vendor contract. Kept separate from our own RiskBand so the mock vendor
 * doesn't depend on our application code, just like the real vendor won't.
 */
public enum VendorRiskBand {
	LOW, MEDIUM, HIGH
}
