#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import ${package}.config.Config;
import ${package}.module.BasicModule;
import ${package}.module.WebModule;

@Slf4j
public class Servlet extends HttpServlet {
	private me.geso.avans.Dispatcher dispatcher;

	@Override
	public void init() {
		log.info("Initialized Servlet");
		final Injector injector = Guice.createInjector(
			this.buildModule(getServletConfig()),
			new WebModule(getServletContext()));
		dispatcher = new Dispatcher(injector);
		dispatcher.registerPackage(${package}.controller.RootController.class.getPackage());
	}

	private BasicModule buildModule(ServletConfig servletConfig) {
		final Object config = servletConfig.getServletContext().getAttribute("${artifactId}.config");
		if (config != null && config instanceof Config) {
			return new BasicModule((Config)config);
		} else {
			return new BasicModule();
		}
	}

	@Override
	public void service(final ServletRequest req, final ServletResponse res)
			throws ServletException, IOException {
		this.dispatcher.handler(
			(HttpServletRequest)req,
			(HttpServletResponse)res);
	}

}
