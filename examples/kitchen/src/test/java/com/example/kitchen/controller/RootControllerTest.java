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
	public void testRoot() {
		TestMechJettyServlet mech = new TestMechJettyServlet(KitchenServlet.class);
		TestMechResponse res = mech.get("/").execute();
		res.assertSuccess();
		res.assertContentTypeContains("text/html");
		res.assertContentContains("<!doctype");
	}

	@Test
	public void testJson() {
		TestMechJettyServlet mech = new TestMechJettyServlet(KitchenServlet.class);
		TestMechResponse res = mech.get("/json").execute();
		res.assertSuccess();
		res.assertContentTypeContains("application/json");
		AvansAPIResponse<String> dat = res.readJSON(new TypeReference<AvansAPIResponse<String>>() {
		});
		assertThat(dat.getCode()).isEqualTo(200);
		assertThat(dat.getData()).isEqualTo("hoge");
	}

}
