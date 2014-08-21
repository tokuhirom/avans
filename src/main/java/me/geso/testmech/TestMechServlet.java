package me.geso.testmech;

import javax.servlet.Servlet;

import lombok.SneakyThrows;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Alpha quaility... I will change API without notice. And so, I want to split
 * the distribution if it's okay.
 * 
 * @author tokuhirom
 *
 */
public class TestMechServlet extends TestMech {
	private Server server;

	@SneakyThrows
	public TestMechServlet(Class<? extends Servlet> servlet) {
		this.server = createServer(servlet);
		this.server.start();
		ServerConnector connector = (ServerConnector) server
				.getConnectors()[0];
		int port = connector.getLocalPort();
		this.setBaseURL("http://127.0.0.1:" + port);
	}

	private Server createServer(Class<? extends Servlet> servlet) {
		int port = 0;
		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(
				server,
				"/",
				ServletContextHandler.SESSIONS
				);
		context.addServlet(servlet, "/*");
		server.setStopAtShutdown(true);
		return server;
	}
}