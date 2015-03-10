#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.junit.BeforeClass;

import ${package}.config.Config;
import ${package}.provider.ConfigProvider;

public class TestBase {
	protected static Config config;

	@BeforeClass
	public static void setupClass() {
		String env = System.getProperty("${artifactId}.env");
		if (env == null) {
			System.setProperty("${artifactId}.env", "test");
		}
		env = System.getProperty("${artifactId}.env");
		if (!(env.equals("test"))) {
			throw new RuntimeException("Do not run test case on non-test environment");
		}

		config = new ConfigProvider().get();
	}
}

