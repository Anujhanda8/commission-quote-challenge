package com.example.commissionquote.common.logging;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.commissionquote.common.AppConstants.Headers;
import com.example.commissionquote.common.AppConstants.Logging;
import com.example.commissionquote.common.AppConstants.Paths;

/**
 * Gives every API request a request ID (taken from the X-Request-Id header or generated), puts it in the
 * logging context so all log lines for the request carry it, returns it in the response, and logs the
 * outcome of the request.
 *
 * Runs first among all filters, so even requests rejected by later filters (e.g. a bad API key) are logged.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Pattern SAFE_REQUEST_ID = Pattern.compile(Logging.SAFE_REQUEST_ID_REGEX);

	private static final SecureLogger log = SecureLogger.getLogger(RequestLoggingFilter.class);

	/** Only API calls are logged; static files for the UI would just add noise. */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return !(uri.startsWith(Paths.API_PREFIX) || uri.startsWith(Paths.MOCK_VENDOR_PREFIX));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String requestId = resolveRequestId(request.getHeader(Headers.REQUEST_ID));
		MDC.put(Logging.REQUEST_ID_MDC_KEY, requestId);
		response.setHeader(Headers.REQUEST_ID, requestId);
		long start = System.currentTimeMillis();
		try {
			chain.doFilter(request, response);
		}
		finally {
			log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(),
					System.currentTimeMillis() - start);
			// Server threads are reused, so the ID must not leak into the next request handled by this thread.
			MDC.remove(Logging.REQUEST_ID_MDC_KEY);
		}
	}

	/** Keeps the caller's ID if it's safe to log; otherwise generates a new one. */
	static String resolveRequestId(String headerValue) {
		if (headerValue != null && SAFE_REQUEST_ID.matcher(headerValue).matches()) {
			return headerValue;
		}
		return UUID.randomUUID().toString();
	}
}
