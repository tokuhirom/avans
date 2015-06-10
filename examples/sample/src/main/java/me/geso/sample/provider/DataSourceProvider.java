package me.geso.sample.provider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import me.geso.sample.config.Config;

public class DataSourceProvider implements Provider<DataSource> {

	private static final int MINIMUM_IDLE =
			System.getProperty("minimumIdle") != null
				? Integer.parseInt(System.getProperty("minimumIdle"))
				: 16;

	private static final int MAXIMUM_POOL_SIZE =
			System.getProperty("maximumPoolSize") != null
				? Integer.parseInt(System.getProperty("maximumPoolSize"))
				: 200;

	private HikariDataSource dataSource;

	@Inject
	public DataSourceProvider(Config config) {
		dataSource = new HikariDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setJdbcUrl(config.getJdbc().getUrl());
		dataSource.setUsername(config.getJdbc().getUsername());
		dataSource.setPassword(config.getJdbc().getPassword());
		dataSource.setAutoCommit(false);
		dataSource.setMinimumIdle(MINIMUM_IDLE);
		dataSource.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
	}

	@Override
	public DataSource get() {
		return dataSource;
	}
}
