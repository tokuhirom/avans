package com.example.kitchen.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import me.geso.avans.APIResponse;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;

import org.junit.Test;

import com.example.kitchen.KitchenServlet;
import com.fasterxml.jackson.core.type.TypeReference;

public class RootControllerTest {

	@Test
	public void testRoot() throws Exception {
		try (MechJettyServlet mech = new MechJettyServlet(
				KitchenServlet.class)) {
			MechResponse res = mech.get("/").execute();
			assertEquals(200, res.getStatusCode());
			assertEquals("text/html", res.getContentType().getMimeType());
			assertTrue(res.getContentString().contains("<!doctype"));
		}
	}

	@Test
	public void testJson() throws Exception {
		try (MechJettyServlet mech = new MechJettyServlet(
				KitchenServlet.class)) {
			MechResponse res = mech.get("/json").execute();
			assertEquals(200, res.getStatusCode());
			assertEquals("application/json", res.getContentType().getMimeType());
			assertEquals("UTF-8", res.getContentType().getCharset().displayName());
			APIResponse<RootController.MyObject> dat = res
					.readJSON(new TypeReference<APIResponse<RootController.MyObject>>() {
					});
			assertThat(dat.getCode()).isEqualTo(200);
			assertThat(dat.getData().getName()).isEqualTo("John");
		}
	}

}
