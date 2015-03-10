#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.cli;

import java.sql.SQLException;

import javax.inject.Inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import ${package}.module.BasicModule;
import ${package}.module.CLIModule;
import me.geso.tinyorm.TinyORM;

@Slf4j
public class SampleCLI {
	public static void main(String[] args) throws SQLException {
		final Injector injector = Guice.createInjector(new BasicModule(), new CLIModule());
		final SampleCLI instance = injector.getInstance(SampleCLI.class);
		instance.run();
	}

	@Inject
	public SampleCLI(final TinyORM db) {
		this.db = db;
	}

	private final TinyORM db;

	public void run() throws SQLException {
		final String databaseProductName = db.getConnection()
			.getMetaData().getDatabaseProductName();
		log.info("{}", databaseProductName);
	}
}
