package com.example.commissionquote.vendor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import tools.jackson.databind.json.JsonMapper;

import com.example.commissionquote.common.AppConstants.ErrorMessages;
import com.example.commissionquote.common.AppConstants.Headers;
import com.example.commissionquote.common.AppConstants.Paths;
import com.example.commissionquote.common.error.ErrorCode;

class VendorApiKeyFilterTest {

	private final VendorApiKeyFilter filter = new VendorApiKeyFilter(
			MockVendorPropertiesFixture.properties(0.0, java.time.Duration.ZERO), JsonMapper.builder().build());

	@Test
	void rejectsMissingApiKey() throws Exception {
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(vendorRequest(null), response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentAsString()).contains(ErrorMessages.INVALID_API_KEY)
			.contains(ErrorCode.MOCK_VENDOR_INVALID_API_KEY.code());
		assertThat(chain.getRequest()).as("request must not reach the controller").isNull();
	}

	@Test
	void rejectsWrongApiKey() throws Exception {
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(vendorRequest("wrong-key"), response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(chain.getRequest()).isNull();
	}

	@Test
	void allowsValidApiKey() throws Exception {
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(vendorRequest(MockVendorPropertiesFixture.API_KEY), response, chain);

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void ignoresNonVendorPaths() throws Exception {
		MockFilterChain chain = new MockFilterChain();
		MockHttpServletRequest request = new MockHttpServletRequest("POST", Paths.QUOTES);

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(chain.getRequest()).isNotNull();
	}

	private static MockHttpServletRequest vendorRequest(String apiKey) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", Paths.MOCK_VENDOR_BASE + Paths.MOCK_VENDOR_QUOTES);
		if (apiKey != null) {
			request.addHeader(Headers.API_KEY, apiKey);
		}
		return request;
	}
}
