package me.geso.sample;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import me.geso.sample.module.BasicModule;

@Slf4j
public class Servlet extends HttpServlet {
	private me.geso.avans.Dispatcher dispatcher;

	public Servlet() {
		log.info("Initialized Servlet");
		final Injector injector = Guice.createInjector(new BasicModule());
		dispatcher = new Dispatcher(injector);
		dispatcher.registerPackage(me.geso.sample.controller.RootController.class.getPackage());
	}

	@Override
	public void service(final ServletRequest req, final ServletResponse res)
			throws ServletException, IOException {
		this.dispatcher.handler(
			(HttpServletRequest)req,
			(HttpServletResponse)res);
	}

}
