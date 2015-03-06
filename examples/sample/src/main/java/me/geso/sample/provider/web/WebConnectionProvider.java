package me.geso.sample.provider.web;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import me.geso.sample.config.Config;
import me.geso.sample.filter.ConnectionManagementFilter;
import me.geso.sample.provider.ConnectionProvider;

/**
 * JDBC Connection provider.
 * This class cache the JDBC connection on servlet request object.
 */
@Slf4j
public class WebConnectionProvider implements Provider<Connection> {
	@Inject
	private Config config;
	@Inject
	private HttpServletRequest servletRequest;
	@Inject
	private ConnectionProvider connectionProvider;

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public Connection get() {
		log.info("Getting JDBC connection");
		Connection connection = ConnectionManagementFilter.getConnection(servletRequest);
		if (connection != null) {
			return connection;
		}

		connection = connectionProvider.get();
		ConnectionManagementFilter.setConnection(servletRequest, connection);
		return connection;
	}
}
