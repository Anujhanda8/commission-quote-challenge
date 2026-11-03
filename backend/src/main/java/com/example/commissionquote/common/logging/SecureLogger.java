package com.example.commissionquote.common.logging;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import com.example.commissionquote.common.AppConstants.Logging;
import com.example.commissionquote.common.error.ErrorCode;

/**
 * The logger used by our code. Wraps SLF4J and adds two things:
 * - Every argument is cleaned of line breaks and tabs, so a value can't forge extra log lines (log injection).
 *   The console pattern also strips them, but this keeps our logs safe if the output changes (e.g. a log service).
 * - Errors are logged with their {@link ErrorCode}, which appears on the log line as [code=CQ-xxxx], matching the
 *   code returned to the client.
 *
 * Rules: never pass the API key or loan amounts as arguments.
 */
public final class SecureLogger {

	private static final Pattern UNSAFE_CHARS = Pattern.compile("[\\r\\n\\t]");

	private final Logger delegate;

	private SecureLogger(Logger delegate) {
		this.delegate = delegate;
	}

	public static SecureLogger getLogger(Class<?> type) {
		return new SecureLogger(LoggerFactory.getLogger(type));
	}

	public void info(String message, Object... args) {
		log(Level.INFO, null, null, message, args);
	}

	public void info(ErrorCode code, String message, Object... args) {
		log(Level.INFO, code, null, message, args);
	}

	public void warn(ErrorCode code, String message, Object... args) {
		log(Level.WARN, code, null, message, args);
	}

	public void error(ErrorCode code, Throwable cause, String message, Object... args) {
		log(Level.ERROR, code, cause, message, args);
	}

	private void log(Level level, ErrorCode code, Throwable cause, String message, Object[] args) {
		if (code != null) {
			MDC.put(Logging.ERROR_CODE_MDC_KEY, code.code());
		}
		try {
			LoggingEventBuilder event = delegate.atLevel(level).setMessage(message);
			for (Object arg : clean(args)) {
				event = event.addArgument(arg);
			}
			if (cause != null) {
				event = event.setCause(cause);
			}
			event.log();
		}
		finally {
			MDC.remove(Logging.ERROR_CODE_MDC_KEY);
		}
	}

	/** Replaces line breaks and tabs in every argument with '_'. */
	static Object[] clean(Object[] args) {
		return Arrays.stream(args)
			.map(arg -> arg == null ? null : UNSAFE_CHARS.matcher(String.valueOf(arg)).replaceAll("_"))
			.toArray();
	}
}
