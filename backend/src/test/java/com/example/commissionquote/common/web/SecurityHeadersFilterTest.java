package com.example.commissionquote.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.commissionquote.common.AppConstants.SecurityHeaders;

class SecurityHeadersFilterTest {

	@Test
	void addsSecurityHeadersAndContinues() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		new SecurityHeadersFilter().doFilter(new MockHttpServletRequest("GET", "/"), response, chain);

		assertThat(response.getHeader("Content-Security-Policy")).isEqualTo(SecurityHeaders.CONTENT_SECURITY_POLICY);
		assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo(SecurityHeaders.CONTENT_TYPE_OPTIONS);
		assertThat(response.getHeader("X-Frame-Options")).isEqualTo(SecurityHeaders.FRAME_OPTIONS);
		assertThat(response.getHeader("Referrer-Policy")).isEqualTo(SecurityHeaders.REFERRER_POLICY);
		assertThat(chain.getRequest()).isNotNull();
	}
}
