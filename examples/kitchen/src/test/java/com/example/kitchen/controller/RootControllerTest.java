package com.example.kitchen.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;

import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RootControllerTest {

	private Tomcat tomcat;
	private Mech2WithBase mech2;

	@Before
	public void before() throws LifecycleException, ServletException,
			URISyntaxException {
		this.tomcat = new Tomcat();
		tomcat.setPort(0);
		org.apache.catalina.Context webContext = tomcat.addWebapp("/",
				new File("src/main/webapp").getAbsolutePath());
		webContext.getServletContext().setAttribute(Globals.ALT_DD_ATTR,
				"src/main/webapp/WEB-INF/web.xml");
		tomcat.start();

		int port = tomcat.getConnector().getLocalPort();
		String url = "http://127.0.0.1:" + port;
		this.mech2 = new Mech2WithBase(Mech2.builder().build(), new URI(url));
	}

	@After
	public void after() throws Exception {
		if (this.tomcat != null) {
			this.tomcat.stop();
		}
	}

	@Test
	public void testRoot() throws Exception {
		Mech2Result res = this.mech2.get("/").execute();
		assertEquals(200, res.getResponse().getStatusLine().getStatusCode());
		assertEquals("text/html",
				res.getResponse().getFirstHeader("Content-Type"));
		assertTrue(res.getResponseBodyAsString().contains("<!doctype"));
	}

	@Test
	public void testJson() throws Exception {
		Mech2Result res = this.mech2.get("/json").execute();
		assertEquals(200, res.getResponse().getStatusLine().getStatusCode());
		ContentType ct = ContentType.parse(
				res.getResponse().getFirstHeader("Content-Type")
						.getValue());
		assertEquals(
				"application/json", ct.getMimeType());
		assertEquals("UTF-8", ct.getCharset().displayName());
		RootController.MyObjectAPIResponse dat = res
				.parseJSON(RootController.MyObjectAPIResponse.class);
		assertThat(dat.getCode(), is(200));
		assertThat(dat.getData().getName(), is("John"));
	}
}
