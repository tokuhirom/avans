package com.example.helloworld;

import org.eclipse.jetty.server.Server;

import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.avans.jetty.JettyServerBuilder;
import me.geso.webscrew.response.WebResponse;

public class Main {
	public static class Foo extends ControllerBase {
		@GET("/")
		public WebResponse hello() {
			return this.renderText("Hello, world");
		}
	}

	public static void main(String[] args) throws Exception {
		Server server = new JettyServerBuilder()
				.setPort(21110)
				.registerClass(Foo.class)
				.build();
		server.start();
		server.join();
	}
}
