package com.example.commissionquote.common.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.commissionquote.common.AppConstants.SecurityHeaders;

/**
 * Adds browser security headers to every response (the UI, the API and errors alike).
 * Set before the request is handled, so they're present even if a later step fails.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		response.setHeader("Content-Security-Policy", SecurityHeaders.CONTENT_SECURITY_POLICY);
		response.setHeader("X-Content-Type-Options", SecurityHeaders.CONTENT_TYPE_OPTIONS);
		response.setHeader("X-Frame-Options", SecurityHeaders.FRAME_OPTIONS);
		response.setHeader("Referrer-Policy", SecurityHeaders.REFERRER_POLICY);
		chain.doFilter(request, response);
	}
}
