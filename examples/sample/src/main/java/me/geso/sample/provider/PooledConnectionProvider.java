package me.geso.sample.provider;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * This is a JDBC connection provider.
 */
@Slf4j
public class PooledConnectionProvider implements Provider<Connection> {

	@Inject
	private DataSource dataSource;

	private Connection connection;

	@Override
	public Connection get() {
		try {
			if (connection == null) {
				connection = dataSource.getConnection();

				log.debug("Get JDBC connection from connection pool: {}",
					connection.getMetaData().getURL());
			}
			return connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
