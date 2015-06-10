#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.module;

import java.sql.Connection;

import com.google.inject.AbstractModule;

import ${package}.provider.ConnectionProvider;
import ${package}.provider.TinyORMProvider;
import me.geso.tinyorm.TinyORM;

public class CLIModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(Connection.class)
			.toProvider(ConnectionProvider.class);
		bind(TinyORM.class)
			.toProvider(TinyORMProvider.class);
	}
}
