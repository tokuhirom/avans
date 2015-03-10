#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.cli;

import java.sql.SQLException;

import org.junit.Test;

import ${package}.TestBase;

public class SampleCLITest extends TestBase {
	@Test
	public void test() throws SQLException {
		SampleCLI.main(new String[]{});
	}

}
