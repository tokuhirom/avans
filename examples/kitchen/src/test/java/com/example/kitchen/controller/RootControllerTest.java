package com.example.kitchen.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;

import org.junit.Test;

import com.example.kitchen.KitchenServlet;

public class RootControllerTest {

	@Test
	public void testRoot() throws Exception {
		try (MechJettyServlet mech = new MechJettyServlet(
				KitchenServlet.class)) {
			try (MechResponse res = mech.get("/").execute()) {
                assertEquals(200, res.getStatusCode());
                assertEquals("text/html", res.getContentType().getMimeType());
                assertTrue(res.getContentString().contains("<!doctype"));
            }
		}
	}

	@Test
	public void testJson() throws Exception {
		try (MechJettyServlet mech = new MechJettyServlet(
				KitchenServlet.class)) {
			try (MechResponse res = mech.get("/json").execute()) {
                assertEquals(200, res.getStatusCode());
                assertEquals("application/json", res.getContentType().getMimeType());
                assertEquals("UTF-8", res.getContentType().getCharset().displayName());
				RootController.MyObjectAPIResponse dat = res
						.readJSON(RootController.MyObjectAPIResponse.class);
                assertThat(dat.getCode()).isEqualTo(200);
                assertThat(dat.getData().getName()).isEqualTo("John");
            }
		}
	}

}
