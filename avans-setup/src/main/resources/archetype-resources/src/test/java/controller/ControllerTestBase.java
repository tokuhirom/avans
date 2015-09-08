#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2WithBase;
import ${package}.TestBase;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.URI;

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
		// servletContext.setAttribute("${artifactId}.config", config);
		// tomcat.start();
		server.setStopAtShutdown(true);
		server.start();

		int port = server.getConnectors()[0].getLocalPort();

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
