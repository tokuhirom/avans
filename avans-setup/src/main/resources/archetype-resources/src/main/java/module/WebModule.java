#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.module;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import ${package}.provider.ConnectionProvider;
import ${package}.provider.TinyORMProvider;
import me.geso.tinyorm.TinyORM;

public class WebModule extends AbstractModule {
	private final HttpServletRequest request;
	private final ConnectionProvider connectionProvider;

	public WebModule(final HttpServletRequest request, final ConnectionProvider connectionProvider) {
		this.request = request;
		this.connectionProvider = connectionProvider;
	}

	@Override
	protected void configure() {
		bind(HttpServletRequest.class)
			.toInstance(request);
		bind(Connection.class)
			.toProvider(connectionProvider)
			.in(Scopes.SINGLETON);
		bind(TinyORM.class)
			.toProvider(TinyORMProvider.class);
	}
}
