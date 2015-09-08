package me.geso.sample.controller;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2WithBase;
import me.geso.sample.TestBase;

public abstract class ControllerTestBase extends TestBase {
	private static Mech2WithBase mech;
	private static Server server;

	@BeforeClass
	public static void controllerTestBaseBeforeClass() throws Exception {
		ControllerTestBase.server = new Server(0);

		WebAppContext webapp = new WebAppContext("", "/");
		webapp.setResourceBase("src/main/webapp/");
		webapp.setHandler(new StatisticsHandler());
		webapp.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		server.setHandler(webapp);

		// servletContext.setAttribute(Globals.ALT_DD_ATTR, "src/main/webapp/WEB-INF/web.xml");
		// Inject configuration into servlet context.
		// servletContext.setAttribute("sample.config", config);
		// tomcat.start();
		server.setStopAtShutdown(true);
		server.start();

		ServerConnector connector = (ServerConnector)server.getConnectors()[0];
		int port = connector.getLocalPort();

		String url = "http://127.0.0.1:" + port;
		ControllerTestBase.mech = new Mech2WithBase(Mech2.builder().build(), new URI(url));
	}

	@AfterClass
	public static void controllerTestBaseAfterClass() throws Exception {
		server.stop();
	}

	protected static Mech2WithBase mech() {
		return mech;
	}
}
