package com.example.commissionquote.quote.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

import com.example.commissionquote.common.AppConstants.Headers;
import com.example.commissionquote.common.AppConstants.Logging;
import com.example.commissionquote.common.error.ErrorCode;
import com.example.commissionquote.quote.QuoteRequest;
import com.example.commissionquote.quote.QuoteResponse;
import com.example.commissionquote.quote.RiskBand;

/**
 * Tests the vendor client against a tiny local HTTP server (JDK built-in), so each vendor behaviour
 * can be controlled precisely without starting Spring.
 */
class CommissionVendorClientTest {

	private static final String QUOTE_PATH = "/commission-quotes";

	private static final QuoteRequest REQUEST = new QuoteRequest(new BigDecimal("10000"), 12, RiskBand.LOW);

	private HttpServer server;

	private final AtomicReference<HttpExchange> lastExchange = new AtomicReference<>();

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.start();
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
		MDC.clear();
	}

	@Test
	void sendsApiKeyAndParsesQuote() {
		respondWith(200, "{\"quoteId\":\"q-1\",\"commissionRate\":1.00,\"totalCommission\":100.00}");

		QuoteResponse quote = client(Duration.ofSeconds(2)).requestQuote(REQUEST);

		assertThat(quote.quoteId()).isEqualTo("q-1");
		assertThat(quote.totalCommission()).isEqualByComparingTo("100.00");
		assertThat(lastExchange.get().getRequestHeaders().getFirst(Headers.API_KEY)).isEqualTo("test-key");
	}

	@Test
	void forwardsRequestIdFromLoggingContext() {
		respondWith(200, "{\"quoteId\":\"q-1\",\"commissionRate\":1.00,\"totalCommission\":100.00}");
		MDC.put(Logging.REQUEST_ID_MDC_KEY, "req-42");

		client(Duration.ofSeconds(2)).requestQuote(REQUEST);

		assertThat(lastExchange.get().getRequestHeaders().getFirst(Headers.REQUEST_ID))
			.isEqualTo("req-42");
	}

	@Test
	void vendorServerErrorBecomesVendorError() {
		respondWith(500, "{\"message\":\"boom\"}");

		assertThatThrownBy(() -> client(Duration.ofSeconds(2)).requestQuote(REQUEST))
			.isInstanceOfSatisfying(VendorException.class,
					ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VENDOR_ERROR));
	}

	@Test
	void vendorRejectingOurApiKeyBecomesAuthFailure() {
		respondWith(401, "{\"message\":\"Invalid or missing API key\"}");

		assertThatThrownBy(() -> client(Duration.ofSeconds(2)).requestQuote(REQUEST))
			.isInstanceOfSatisfying(VendorException.class,
					ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VENDOR_AUTH_FAILED));
	}

	@ParameterizedTest
	@ValueSource(strings = { "{}", "{\"quoteId\":null,\"commissionRate\":null,\"totalCommission\":null}", "" })
	void emptyVendorResponseBecomesEmptyResponseError(String body) {
		respondWith(200, body);

		assertThatThrownBy(() -> client(Duration.ofSeconds(2)).requestQuote(REQUEST))
			.isInstanceOfSatisfying(VendorException.class,
					ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VENDOR_EMPTY_RESPONSE));
	}

	@ParameterizedTest
	@ValueSource(strings = { "{\"quoteId\":\"q-1\",\"commissionRate\":1.00}",
			"{\"quoteId\":\" \",\"commissionRate\":1.00,\"totalCommission\":100.00}",
			"{\"quoteId\":\"q-1\",\"commissionRate\":-1.00,\"totalCommission\":100.00}" })
	void incompleteOrNegativeQuoteBecomesVendorError(String body) {
		respondWith(200, body);

		assertThatThrownBy(() -> client(Duration.ofSeconds(2)).requestQuote(REQUEST))
			.isInstanceOfSatisfying(VendorException.class,
					ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VENDOR_ERROR));
	}

	@Test
	void slowVendorBecomesTimeout() {
		server.createContext(QUOTE_PATH, exchange -> {
			sleep(1000);
			send(exchange, 200, "{}");
		});

		assertThatThrownBy(() -> client(Duration.ofMillis(200)).requestQuote(REQUEST))
			.isInstanceOfSatisfying(VendorException.class,
					ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VENDOR_TIMEOUT));
	}

	@Test
	void unreachableVendorBecomesVendorError() {
		int port = server.getAddress().getPort();
		server.stop(0);
		VendorProperties properties = properties("http://localhost:" + port, Duration.ofSeconds(1));

		assertThatThrownBy(() -> new CommissionVendorClient(properties).requestQuote(REQUEST))
			.isInstanceOfSatisfying(VendorException.class,
					ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VENDOR_ERROR));
	}

	@Test
	void malformedVendorResponseBecomesVendorError() {
		respondWith(200, "not json");

		assertThatThrownBy(() -> client(Duration.ofSeconds(2)).requestQuote(REQUEST))
			.isInstanceOfSatisfying(VendorException.class,
					ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VENDOR_ERROR));
	}

	private CommissionVendorClient client(Duration timeout) {
		return new CommissionVendorClient(properties("http://localhost:" + server.getAddress().getPort(), timeout));
	}

	private static VendorProperties properties(String baseUrl, Duration readTimeout) {
		return new VendorProperties(baseUrl, QUOTE_PATH, "test-key", Duration.ofSeconds(1), readTimeout);
	}

	private void respondWith(int status, String json) {
		server.createContext(QUOTE_PATH, exchange -> {
			lastExchange.set(exchange);
			send(exchange, status, json);
		});
	}

	private static void send(HttpExchange exchange, int status, String json) throws IOException {
		byte[] body = json.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(body);
		}
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
