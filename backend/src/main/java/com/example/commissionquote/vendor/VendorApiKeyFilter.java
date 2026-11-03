package com.example.commissionquote.vendor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.json.JsonMapper;

import com.example.commissionquote.common.AppConstants.Config;
import com.example.commissionquote.common.AppConstants.Headers;
import com.example.commissionquote.common.AppConstants.Paths;
import com.example.commissionquote.common.error.ErrorCode;
import com.example.commissionquote.common.error.ErrorResponse;
import com.example.commissionquote.common.logging.SecureLogger;

/**
 * Rejects any mock vendor request without a valid api-key header with 401.
 * Implemented as a filter so it runs before the request body is read or validated.
 */
@Component
@ConditionalOnProperty(name = Config.MOCK_VENDOR_ENABLED, havingValue = "true")
public class VendorApiKeyFilter extends OncePerRequestFilter {

	private static final SecureLogger log = SecureLogger.getLogger(VendorApiKeyFilter.class);

	private final byte[] expectedApiKey;

	private final JsonMapper jsonMapper;

	public VendorApiKeyFilter(MockVendorProperties properties, JsonMapper jsonMapper) {
		this.expectedApiKey = properties.apiKey().getBytes(StandardCharsets.UTF_8);
		this.jsonMapper = jsonMapper;
	}

	/** Only the mock vendor endpoints are protected; our own API and the UI are not. */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith(Paths.MOCK_VENDOR_PREFIX);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String apiKey = request.getHeader(Headers.API_KEY);
		// Constant-time comparison so the key can't be guessed via response timing.
		if (apiKey == null || !MessageDigest.isEqual(expectedApiKey, apiKey.getBytes(StandardCharsets.UTF_8))) {
			ErrorCode code = ErrorCode.MOCK_VENDOR_INVALID_API_KEY;
			// Never log the key itself.
			log.warn(code, "Mock vendor rejected request: {} api-key", apiKey == null ? "missing" : "invalid");
			response.setStatus(code.status().value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write(jsonMapper.writeValueAsString(ErrorResponse.of(code)));
			return;
		}
		chain.doFilter(request, response);
	}
}
