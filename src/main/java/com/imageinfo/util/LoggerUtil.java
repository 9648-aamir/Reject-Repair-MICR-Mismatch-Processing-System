package com.imageinfo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to provide a centralized logger instance for any class. Usage:
 * private static final Logger logger = LoggerUtil.getLogger(MyClass.class);
 */
public final class LoggerUtil {

	private LoggerUtil() {
	}

	public static Logger getLogger(Class<?> clazz) {
		return LogManager.getLogger(clazz);
	}
}
