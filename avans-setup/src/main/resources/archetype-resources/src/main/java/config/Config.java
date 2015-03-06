#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.config;

import lombok.Data;

@Data
public class Config {
    private boolean development;
	private JDBCConfig jdbc;
}
