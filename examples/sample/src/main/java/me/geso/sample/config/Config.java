package me.geso.sample.config;

import lombok.Data;

@Data
public class Config {
	private boolean development;
	private JDBCConfig jdbc;
	private DataSourceConfig dataSource;
}
