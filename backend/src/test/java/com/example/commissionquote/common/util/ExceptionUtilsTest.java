package com.example.commissionquote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;

class ExceptionUtilsTest {

	private final SocketTimeoutException timeout = new SocketTimeoutException("Read timed out");

	private final RuntimeException wrapped = new RuntimeException("outer", new UncheckedIOException(timeout));

	@Test
	void findsCauseDeepInTheChain() {
		assertThat(ExceptionUtils.findCause(wrapped, SocketTimeoutException.class)).isSameAs(timeout);
	}

	@Test
	void matchesTheExceptionItself() {
		assertThat(ExceptionUtils.findCause(timeout, SocketTimeoutException.class)).isSameAs(timeout);
	}

	@Test
	void matchesSubtypes() {
		assertThat(ExceptionUtils.findCause(wrapped, IOException.class)).isSameAs(timeout);
	}

	@Test
	void returnsNullWhenNotInTheChain() {
		assertThat(ExceptionUtils.findCause(wrapped, IllegalStateException.class)).isNull();
		assertThat(ExceptionUtils.hasCause(wrapped, IllegalStateException.class)).isFalse();
		assertThat(ExceptionUtils.hasCause(null, IllegalStateException.class)).isFalse();
	}

	@Test
	void hasCauseIsTrueWhenFound() {
		assertThat(ExceptionUtils.hasCause(wrapped, SocketTimeoutException.class)).isTrue();
	}
}
