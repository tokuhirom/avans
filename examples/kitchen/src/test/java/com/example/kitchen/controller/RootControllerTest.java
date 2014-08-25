package com.example.kitchen.controller;

import static org.assertj.core.api.Assertions.assertThat;
import me.geso.avans.AvansAPIResponse;
import me.geso.testmech.TestMechJettyServlet;
import me.geso.testmech.TestMechResponse;

import org.junit.Test;

import com.example.kitchen.KitchenServlet;
import com.fasterxml.jackson.core.type.TypeReference;

public class RootControllerTest {

	@Test
	public void testRoot() throws Exception {
		try (TestMechJettyServlet mech = new TestMechJettyServlet(
				KitchenServlet.class)) {
			TestMechResponse res = mech.get("/").execute();
			res.assertSuccess();
			res.assertContentTypeMimeTypeEquals("text/html");
			res.assertContentContains("<!doctype");
		}
	}

	@Test
	public void testJson() throws Exception {
		try (TestMechJettyServlet mech = new TestMechJettyServlet(
				KitchenServlet.class)) {
			TestMechResponse res = mech.get("/json").execute();
			res.assertSuccess();
			res.assertContentTypeMimeTypeEquals("application/json");
			res.assertContentTypeCharsetEquals("UTF-8");
			AvansAPIResponse<RootController.MyObject> dat = res
					.readJSON(new TypeReference<AvansAPIResponse<RootController.MyObject>>() {
					});
			assertThat(dat.getCode()).isEqualTo(200);
			assertThat(dat.getData().getName()).isEqualTo("John");
		}
	}

}
