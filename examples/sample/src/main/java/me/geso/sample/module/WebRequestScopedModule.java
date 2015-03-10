package me.geso.sample.module;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import me.geso.sample.provider.ConnectionProvider;
import me.geso.sample.provider.TinyORMProvider;
import me.geso.tinyorm.TinyORM;

public class WebRequestScopedModule extends AbstractModule {
	private final HttpServletRequest request;
	private final ConnectionProvider connectionProvider;

	public WebRequestScopedModule(final HttpServletRequest request, final ConnectionProvider connectionProvider) {
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
