package com.example.kitchen;

import com.example.kitchen.controller.RootController;
import me.geso.avans.jetty.JettyServerBuilder;
import org.eclipse.jetty.server.Server;

public class Main {
	public static void main(String[] args) throws Exception {
		new Main().doMain();
	}

	private static final int port = 8080;

	private void doMain() throws Exception {
		Server server = new JettyServerBuilder()
				.setPort(port)
				.setMaxThreads(10)
				.setMinThreads(10)
				.registerPackage(RootController.class.getPackage())
				.build();
		server.start();
		server.join();
	}

}
