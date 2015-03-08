package me.geso.sample.provider;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.slf4j.Slf4j;
import me.geso.sample.config.Config;

/**
 * This is a JDBC connection provider.
 * This class implements Closeable interface.
 * You can close the connection after work.
 */
@Slf4j
public class ConnectionProvider implements Provider<Connection>, Closeable {

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private final Config config;
	private Connection connection;

	@Inject
	public ConnectionProvider(Config config) {
		this.config = config;
	}

	public Connection get() {
		try {
			log.debug("Creating new JDBC connection: {}", config.getJdbc().getUrl());
			this.connection = DriverManager.getConnection(
					config.getJdbc().getUrl(),
					config.getJdbc().getUsername(),
					config.getJdbc().getPassword());
			return this.connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	// Close last connection.
	@Override
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			log.debug("Closed DB Connection");
		}
	}
}
