package com.example.commissionquote.quote.client;

import java.net.SocketTimeoutException;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.example.commissionquote.common.AppConstants.Headers;
import com.example.commissionquote.common.AppConstants.Logging;
import com.example.commissionquote.common.error.ErrorCode;
import com.example.commissionquote.common.util.ExceptionUtils;
import com.example.commissionquote.quote.QuoteRequest;
import com.example.commissionquote.quote.QuoteResponse;

/**
 * Calls the third-party Commission Quote API. The api-key is added here on the server,
 * so it is never exposed to the browser. All failures are translated into {@link VendorException}.
 *
 * The vendor contract currently matches our own request/response, so our DTOs are sent as they are.
 * Map them explicitly here if the contracts ever differ.
 */
@Component
public class CommissionVendorClient {

	private final RestClient restClient;

	private final String quotePath;

	public CommissionVendorClient(VendorProperties properties) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(properties.connectTimeout());
		requestFactory.setReadTimeout(properties.readTimeout());
		this.quotePath = properties.quotePath();
		this.restClient = RestClient.builder()
			.baseUrl(properties.baseUrl())
			.defaultHeader(Headers.API_KEY, properties.apiKey())
			.requestFactory(requestFactory)
			// Pass our request ID on, so vendor-side logs can be matched to ours.
			.requestInterceptor((request, body, execution) -> {
				String requestId = MDC.get(Logging.REQUEST_ID_MDC_KEY);
				if (requestId != null) {
					request.getHeaders().set(Headers.REQUEST_ID, requestId);
				}
				return execution.execute(request, body);
			})
			.build();
	}

	/**
	 * Requests a quote from the vendor.
	 *
	 * @throws VendorException if the vendor times out, rejects our API key, returns an error, or returns an
	 * empty or unusable quote. Logged by ApiExceptionHandler.
	 */
	public QuoteResponse requestQuote(QuoteRequest request) {
		QuoteResponse response;
		try {
			response = restClient.post()
				.uri(quotePath)
				.body(request)
				.retrieve()
				.body(QuoteResponse.class);
		}
		catch (RestClientResponseException ex) {
			// The vendor answered with a 4xx/5xx status.
			int status = ex.getStatusCode().value();
			if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
				throw new VendorException(ErrorCode.VENDOR_AUTH_FAILED,
						"Commission vendor rejected our API key (HTTP " + status + ")", ex);
			}
			throw new VendorException(ErrorCode.VENDOR_ERROR, "Commission vendor returned HTTP " + status, ex);
		}
		catch (RestClientException ex) {
			// A timeout can surface wrapped in different exception types (e.g. while reading the response),
			// so check the whole cause chain rather than one exception type.
			if (ExceptionUtils.hasCause(ex, SocketTimeoutException.class)) {
				throw new VendorException(ErrorCode.VENDOR_TIMEOUT, "Commission vendor timed out", ex);
			}
			throw new VendorException(ErrorCode.VENDOR_ERROR, "Commission vendor call failed: " + ex.getMessage(), ex);
		}
		return checked(response);
	}

	/**
	 * A 200 doesn't guarantee a usable quote. No body or {} is an empty response; missing or negative values are an
	 * invalid one. Either way the user must not be shown a broken quote.
	 */
	private static QuoteResponse checked(QuoteResponse response) {
		if (response == null || (response.quoteId() == null && response.commissionRate() == null
				&& response.totalCommission() == null)) {
			throw new VendorException(ErrorCode.VENDOR_EMPTY_RESPONSE, "Commission vendor returned an empty response",
					null);
		}
		boolean complete = response.quoteId() != null && !response.quoteId().isBlank()
				&& response.commissionRate() != null && response.totalCommission() != null;
		if (!complete || response.commissionRate().signum() < 0 || response.totalCommission().signum() < 0) {
			throw new VendorException(ErrorCode.VENDOR_ERROR, "Commission vendor returned an incomplete or invalid quote",
					null);
		}
		return response;
	}
}
