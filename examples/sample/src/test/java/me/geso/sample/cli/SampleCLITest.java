package me.geso.sample.cli;

import java.sql.SQLException;

import org.junit.Test;

import me.geso.sample.TestBase;

public class SampleCLITest extends TestBase {
	@Test
	public void test() throws SQLException {
		SampleCLI.main(new String[]{});
	}

}
