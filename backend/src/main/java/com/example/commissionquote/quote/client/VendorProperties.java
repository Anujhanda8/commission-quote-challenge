package com.example.commissionquote.quote.client;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Connection settings for the third-party Commission Quote API (the "vendor" section of application.yml).
 * Validated at startup, so a missing setting stops the app immediately instead of failing on the first request.
 *
 * @param baseUrl root URL of the vendor API
 * @param quotePath path of the quote endpoint, relative to baseUrl
 * @param apiKey key sent in the api-key header
 * @param connectTimeout maximum time to open a connection
 * @param readTimeout maximum time to wait for the vendor's response
 */
@Validated
@ConfigurationProperties(prefix = "vendor")
public record VendorProperties(
		@NotBlank String baseUrl,
		@NotBlank String quotePath,
		@NotBlank String apiKey,
		@NotNull Duration connectTimeout,
		@NotNull Duration readTimeout) {
}
