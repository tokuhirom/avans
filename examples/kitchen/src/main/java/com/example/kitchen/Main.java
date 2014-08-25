package com.example.kitchen;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class Main {
	public static void main(String[] args) throws Exception {
		new Main().doMain();
	}

	final int port = 8080;
	final int maxThreads = 80;
	final int minThreads = maxThreads;

	private void doMain() throws Exception {
		QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads,
				minThreads);

		Server server = new Server(threadPool);
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });
		ServletContextHandler context = new ServletContextHandler(
				server,
				"/",
				ServletContextHandler.SESSIONS
				);
		context.addServlet(KitchenServlet.class, "/*");
		server.setStopAtShutdown(true);
		server.start();
		server.join();
	}

}
