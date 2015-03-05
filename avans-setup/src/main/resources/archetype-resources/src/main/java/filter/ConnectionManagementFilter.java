#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.filter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionManagementFilter implements Filter {
	private static final String KEY = "${package}.connection.db";

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(request, response);

		final Object db = request.getAttribute(KEY);
		if (db != null && db instanceof Connection) {
			try {
				log.debug("Closing JDBC connection");
				((Connection)db).close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void destroy() {
	}

	public static Connection getConnection(ServletRequest request) {
		return (Connection)request.getAttribute(KEY);
	}

	public static void setConnection(ServletRequest request, Connection connection) {
		request.setAttribute(KEY, connection);
	}
}
