package com.example.kitchen;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Main {
	public static void main(String[] args) throws Exception {
		int port = 8080;
		Server server = new Server(port);
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
