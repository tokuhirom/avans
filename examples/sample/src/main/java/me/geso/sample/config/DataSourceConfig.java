package me.geso.sample.config;

import lombok.Data;

@Data
public class DataSourceConfig {

	// TODO If you wanto to customize HikariCP's options. you should customize here.

	// connection pool options
	private int minimumIdle;
	private int maximumPoolSize;
}
