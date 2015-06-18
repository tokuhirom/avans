#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.provider;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Provider;

import me.geso.tinyorm.TinyORM;

public class TinyORMProvider implements Provider<TinyORM> {

	@Inject
	private Connection connection;

	@Override
	public TinyORM get() {
		return new TinyORM(connection);
	}
}
