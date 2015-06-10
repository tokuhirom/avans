package me.geso.sample;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class Servlet extends HttpServlet {

	private static final String MULTIPART_CONFIG = "org.eclipse.jetty.multipartConfig";

	private static final String LOCATION = "";

	private static final long MAX_FILE_SIZE = 5_242_880;

	private static final long MAX_REQUEST_SIZE = 27_262_976;

	private static final int FILE_SIZE_THRESHOLD = 32_768;

	private static final MultipartConfigElement MULTIPART_CONFIG_ELEMENT =
			new MultipartConfigElement(LOCATION, MAX_FILE_SIZE, MAX_REQUEST_SIZE, FILE_SIZE_THRESHOLD);

	@Inject
	private Injector injector;

	private Dispatcher dispatcher;

	@Override
	public void init() {
		log.info("Initialized Servlet");
		dispatcher = new Dispatcher(injector);
		dispatcher.registerPackage(me.geso.sample.controller.RootController.class.getPackage());
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		// guice-servlet need this hack.
		// tomcat config is META-INF/context.xml (allowCasualMultipartParsing="true")
		if ("POST".equals(req.getMethod())) {
			req.setAttribute(MULTIPART_CONFIG, MULTIPART_CONFIG_ELEMENT);
		}

		this.dispatcher.handler(req, res);
	}
}
