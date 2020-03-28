package com.raad.converter.util;

import java.util.ArrayList;
import java.util.List;


public class ExceptionUtil {

	public static Throwable getRootCause(final Throwable throwable) {
		final List<Throwable> list = getThrowableList(throwable);
		Throwable rootCause = list.size() < 2 ? null : (Throwable) list.get(list.size() - 1);
		if (rootCause == null) { return throwable; }
		return rootCause;
	}

	public static String getRootCauseMessage(final Throwable throwable) { return getRootCause(throwable).toString(); }

	private static List<Throwable> getThrowableList(Throwable throwable) {
		final List<Throwable> list = new ArrayList<Throwable>();
		while (throwable != null && list.contains(throwable) == false) {
			list.add(throwable);
			throwable = throwable.getCause();
		}
		return list;
	}
}
