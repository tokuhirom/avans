package me.geso.sample.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.slf4j.Slf4j;
import me.geso.sample.config.Config;

/**
 * This is a JDBC connection provider.
 */
@Slf4j
public class ConnectionProvider implements Provider<Connection> {

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject
	private Config config;

	private Connection connection;

	@Override
	public Connection get() {
		try {
			if (connection == null) {
				log.debug("Creating new JDBC connection: {}", config.getJdbc().getUrl());

				connection = DriverManager.getConnection(
					config.getJdbc().getUrl(),
					config.getJdbc().getUsername(),
					config.getJdbc().getPassword());
			}

			return connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
