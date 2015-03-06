#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.module;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import ${package}.provider.TinyORMProvider;
import ${package}.provider.web.WebConnectionProvider;
import me.geso.tinyorm.TinyORM;

/**
 * Created by tokuhirom on 3/6/15.
 */
public class WebModule extends AbstractModule {
	private final HttpServletRequest request;

	public WebModule(final HttpServletRequest request) {
		this.request = request;
	}

	@Override
	protected void configure() {
		bind(HttpServletRequest.class)
			.toInstance(request);
		bind(Connection.class)
			.toProvider(WebConnectionProvider.class)
			.in(Scopes.SINGLETON);
		bind(TinyORM.class)
				.toProvider(TinyORMProvider.class);
	}
}
