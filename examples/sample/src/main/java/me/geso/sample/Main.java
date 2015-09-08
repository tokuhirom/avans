package me.geso.sample;

import org.eclipse.jetty.server.Server;

import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import me.geso.sample.module.JettyModule;

@Slf4j
public class Main {
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new JettyModule());
		Server server = injector.getInstance(Server.class);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			log.error("Cannot start jetty server", e);
		}
	}
}
