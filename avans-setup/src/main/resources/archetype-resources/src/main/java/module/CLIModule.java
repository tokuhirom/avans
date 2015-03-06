#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.module;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import ${package}.provider.ConnectionProvider;
import ${package}.provider.TinyORMProvider;
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
