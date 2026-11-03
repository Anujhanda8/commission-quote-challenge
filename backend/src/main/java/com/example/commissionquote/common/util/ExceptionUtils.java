package com.example.commissionquote.common.util;

/**
 * Helpers for inspecting exceptions. Libraries often wrap the real cause in other exceptions (and the wrapping can
 * differ between versions), so checking only the outer exception type is fragile.
 */
public final class ExceptionUtils {

	private ExceptionUtils() {
	}

	/**
	 * Returns the first exception of the given type in the cause chain, starting with {@code ex} itself,
	 * or {@code null} if there is none.
	 */
	public static <T extends Throwable> T findCause(Throwable ex, Class<T> type) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (type.isInstance(cause)) {
				return type.cast(cause);
			}
		}
		return null;
	}

	/** Whether {@code ex} or any of its causes is of the given type. */
	public static boolean hasCause(Throwable ex, Class<? extends Throwable> type) {
		return findCause(ex, type) != null;
	}
}
