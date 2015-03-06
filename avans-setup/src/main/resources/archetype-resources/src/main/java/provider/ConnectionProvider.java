#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;

import ${package}.config.Config;

/**
 * In CLI, we should create
 */
public class ConnectionProvider implements Provider<Connection> {
	@Inject
	private Config config;

	public Connection get() {
		try {
			return DriverManager.getConnection(
					config.getJdbc().getUrl(),
					config.getJdbc().getUsername(),
					config.getJdbc().getPassword());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
