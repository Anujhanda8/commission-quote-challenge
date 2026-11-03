package com.example.commissionquote.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.example.commissionquote.common.AppConstants.Logging;
import com.example.commissionquote.common.error.ErrorCode;

/** Isolated: other tests starting Spring would reconfigure logging and detach the test appender. */
@Isolated
class SecureLoggerTest {

	private final SecureLogger log = SecureLogger.getLogger(SecureLoggerTest.class);

	private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

	private final Logger logbackLogger = (Logger) LoggerFactory.getLogger(SecureLoggerTest.class);

	@BeforeEach
	void attachAppender() {
		appender.start();
		logbackLogger.addAppender(appender);
	}

	@AfterEach
	void detachAppender() {
		logbackLogger.detachAppender(appender);
	}

	@Test
	void lineBreaksInArgumentsCannotForgeLogLines() {
		log.info("Field {}", "riskBand\nINFO fake log line\r\tend");

		assertThat(appender.list.get(0).getFormattedMessage()).isEqualTo("Field riskBand_INFO fake log line__end");
	}

	@Test
	void errorCodeIsOnTheLogEventAndClearedAfterwards() {
		log.warn(ErrorCode.VENDOR_TIMEOUT, "Commission vendor timed out");

		assertThat(appender.list.get(0).getMDCPropertyMap())
			.containsEntry(Logging.ERROR_CODE_MDC_KEY, ErrorCode.VENDOR_TIMEOUT.code());
		assertThat(MDC.get(Logging.ERROR_CODE_MDC_KEY)).isNull();
	}

	@Test
	void errorsKeepTheirCause() {
		IllegalStateException cause = new IllegalStateException("boom");

		log.error(ErrorCode.INTERNAL_ERROR, cause, "Unexpected error");

		assertThat(appender.list.get(0).getThrowableProxy().getMessage()).isEqualTo("boom");
	}

	@Test
	void plainArgumentsAreUnchanged() {
		assertThat(SecureLogger.clean(new Object[] { 72, "MEDIUM", null })).containsExactly("72", "MEDIUM", null);
	}
}
