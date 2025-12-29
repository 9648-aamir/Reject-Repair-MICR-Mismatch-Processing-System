package com.imageinfo.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Log4jInitListener implements ServletContextListener {

	private static final Logger logger = LoggerUtil.getLogger(Log4jInitListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		String webAppPath = context.getRealPath("/");

		String log4jConfigFile = webAppPath + "WEB-INF/conf/log4j2.properties";
		File file = new File(log4jConfigFile);

		if (file.exists()) {
			Configurator.initialize(null, log4jConfigFile);
			logger.info("Log4j2 initialized from: {}", log4jConfigFile);
		} else {
			logger.info("Log4j2 properties file not found: {}", log4jConfigFile);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		/** Nothing needed here */
	}
}
