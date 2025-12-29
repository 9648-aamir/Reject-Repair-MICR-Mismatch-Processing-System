package com.imageinfo.connection;


import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;
import org.zkoss.zk.ui.Sessions;

public class DataBaseConnection {

	private static DataSource dataSource;

	private DataBaseConnection() {
	}

	private static void initializeDataSource() throws SQLException {
		Properties props = new Properties();

		try (InputStream input = Sessions.getCurrent().getWebApp()
				.getResourceAsStream("/WEB-INF/conf/connection.properties")) {

			if (input == null) {
				throw new SQLException("Connection properties file not found!");
			}

			props.load(input);

			String url = props.getProperty("db.url");
			String username = props.getProperty("db.username");
			String password = props.getProperty("db.password");

			PGSimpleDataSource pgDS = new PGSimpleDataSource();
			pgDS.setUrl(url);
			pgDS.setUser(username);
			pgDS.setPassword(password);

			dataSource = pgDS;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			initializeDataSource();
		}

		if (dataSource == null) {
			throw new SQLException("DataSource could not be initialized! Check DB properties file.");
		}

		return dataSource.getConnection();
	}
}
