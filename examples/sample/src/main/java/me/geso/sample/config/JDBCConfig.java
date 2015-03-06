package me.geso.sample.config;

import lombok.Data;

@Data
public class JDBCConfig {
	private String url;
	private String username;
	private String password;
}
