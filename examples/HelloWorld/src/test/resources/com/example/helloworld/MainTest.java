package com.example.helloworld;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import me.geso.avans.AvansServlet;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MainTest {
	private MechJettyServlet mech;

	@Before
	public void before() {
		AvansServlet servlet = new AvansServlet();
		servlet.registerClass(com.example.helloworld.Main.Foo.class);
		this.mech = new MechJettyServlet(servlet);
	}

	@After
	public void after() throws Exception {
		if (this.mech != null) {
			this.mech.close();
		}
	}

	@Test
	public void test() throws Exception {
		try (MechResponse res = mech.get("/").execute()) {
			assertThat(res.getStatusCode(), is(200));
			assertTrue(res.getContentString().contains("Hello"));
		}
	}

}
