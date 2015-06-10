#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import ${package}.config.Config;
import ${package}.module.BasicModule;
import ${package}.module.WebModule;
import ${package}.module.WebRequestScopedModule;

public class GuiceServletConfig extends GuiceServletContextListener {

	private ServletContext servletContext;

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
	}

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(
			buildBasicModule(),
			new WebModule(),
			new WebRequestScopedModule(),
			new ServletModule() {
				@Override
				protected void configureServlets() {
					serve("/static/*", "/components/*").with(DefaultServlet.class);
					serve("/*").with(Servlet.class);
				}
			});
	}

	BasicModule buildBasicModule() {
		Object config = servletContext.getAttribute("${artifactId}.config");
		if (config != null && config instanceof Config) {
			return new BasicModule((Config)config);
		} else {
			return new BasicModule();
		}
	}
}
