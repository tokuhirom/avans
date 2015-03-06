#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.config;

import lombok.Data;

@Data
public class JDBCConfig {
	private String url;
	private String username;
	private String password;
}
