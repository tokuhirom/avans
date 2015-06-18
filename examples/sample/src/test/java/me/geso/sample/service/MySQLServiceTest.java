package me.geso.sample.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import me.geso.sample.TestBase;

public class MySQLServiceTest extends TestBase {

	MySQLService sut = getInjector().getInstance(MySQLService.class);

	@Test
	public void testJDBCMajorVersion() throws Exception {
		assertThat(sut.getJDBCMajorVersion(), is(4));
	}
}
