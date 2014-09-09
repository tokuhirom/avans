package me.geso.avans.jetty;

import me.geso.avans.AvansServlet;
import me.geso.avans.Controller;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Created by tokuhirom on 9/6/14.
 */
public class JettyServerBuilder {
	private int port = 80;
	private final AvansServlet servlet;
	private int minThreads = 80;
	private int maxThreads = 80;
	private boolean accessLogEnabled;

	public JettyServerBuilder() {
		this.servlet = new AvansServlet();
	}

	public JettyServerBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public JettyServerBuilder registerPackage(Package pkg) {
		this.servlet.registerPackage(pkg);
		return this;
	}

	public JettyServerBuilder registerClass(Class<? extends Controller> klass) {
		this.servlet.registerClass(klass);
		return this;
	}

	public JettyServerBuilder enableAccessLog() {
		this.accessLogEnabled = true;
		return this;
	}

	public Server build() {
		QueuedThreadPool queuedThreadPool = new QueuedThreadPool(
				this.minThreads, this.maxThreads);
		Server server = new Server(queuedThreadPool);
		if (this.accessLogEnabled) {
			RequestLogHandler requestLogHandler = new RequestLogHandler();
			requestLogHandler.setRequestLog(new Slf4jRequestLog());
			server.setHandler(requestLogHandler);
		}

		ServerConnector serverConnector = new ServerConnector(server);
		serverConnector.setPort(port);
		server.addConnector(serverConnector);
		ServletHolder servletHolder = new ServletHolder(this.servlet);
		ServletContextHandler context = new ServletContextHandler(
				server,
				"/",
				ServletContextHandler.SESSIONS
				);
		context.addServlet(servletHolder, "/*");
		server.setStopAtShutdown(true);
		return server;
	}

	public JettyServerBuilder setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
		return this;
	}

	public JettyServerBuilder setMinThreads(int minThreads) {
		this.minThreads = minThreads;
		return this;
	}
}
