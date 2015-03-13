package me.geso.sample.controller;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2WithBase;
import me.geso.sample.TestBase;

public abstract class ControllerTestBase extends TestBase {
	private static Tomcat tomcat;
	private static Mech2WithBase mech;

	@BeforeClass
	public static void controllerTestBaseBeforeClass() throws ServletException, LifecycleException, URISyntaxException {
		ControllerTestBase.tomcat = new Tomcat();
		tomcat.setPort(0);
		org.apache.catalina.Context webContext = tomcat.addWebapp("/", new File("src/main/webapp").getAbsolutePath());
		final ServletContext servletContext = webContext.getServletContext();
		servletContext.setAttribute(Globals.ALT_DD_ATTR, "src/main/webapp/WEB-INF/web.xml");
		// Inject configuration into servlet context.
		servletContext.setAttribute("sample.config", config);
		tomcat.start();

		int port = tomcat.getConnector().getLocalPort();
		String url = "http://127.0.0.1:" + port;
		ControllerTestBase.mech = new Mech2WithBase(Mech2.builder().build(), new URI(url));
	}

	@AfterClass
	public static void controllerTestBaseAfterClass()
			throws LifecycleException {
		ControllerTestBase.tomcat.stop();
	}

	protected static Mech2WithBase mech() {
		return mech;
	}

	public Tomcat getTomcat() {
		return ControllerTestBase.tomcat;
	}
}
