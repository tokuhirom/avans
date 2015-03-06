package me.geso.sample.module;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import me.geso.sample.provider.ConnectionProvider;
import me.geso.sample.provider.TinyORMProvider;
import me.geso.tinyorm.TinyORM;

public class CLIModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(Connection.class)
			.toProvider(ConnectionProvider.class)
			.in(Scopes.SINGLETON);
		bind(TinyORM.class)
				.toProvider(TinyORMProvider.class);
	}
}
