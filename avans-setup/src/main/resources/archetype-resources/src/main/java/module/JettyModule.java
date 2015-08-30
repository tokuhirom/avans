#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.module;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JettyModule extends AbstractModule {
	private static final String DUMP_BEFORE_STOP = "jetty.dumpBeforeStop";
	private static final String WEBAPP = "jetty.webApp";
	private static final String HOST = "jetty.host";
	private static final String PORT = "jetty.port";
	private static final String IDLE_TIMEOUT = "jetty.idleTimeout";
	private static final String SO_LINGER_TIME = "jetty.soLingerTime";
	private static final String ACCEPTOR_PRIORITY_DELTA = "jetty.acceptorPriorityDelta";
	private static final String ACCEPT_QUERY_SIZE = "jetty.acceptQuerySize";
	private static final String THREAD_POOL_MAX_THREADS = "jetty.threadPool.maxThreads";
	private static final String THREAD_POOL_MIN_THREADS = "jetty.threadPool.minThreads";
	private static final String THREAD_POOL_DETAILED_DUMP = "jetty.threadPool.detailedDump";
	private static final String STOP_TIMEOUT = "jetty.stopTimeout";
	private static final String INHERIT_CHANNEL = "jetty.inheritChannel";

	@Override
	protected void configure() {
		final HashMap<String, String> defaults = new HashMap<>();

		// Put default values
		defaults.put(DUMP_BEFORE_STOP, "false");
		defaults.put(HOST, "127.0.0.1");
		defaults.put(PORT, "8080");
		defaults.put(IDLE_TIMEOUT, "30000");
		defaults.put(SO_LINGER_TIME, "-1");
		defaults.put(ACCEPTOR_PRIORITY_DELTA, "-1");
		defaults.put(ACCEPT_QUERY_SIZE, "0");
		defaults.put(THREAD_POOL_MAX_THREADS, "200");
		defaults.put(THREAD_POOL_MIN_THREADS, "200");
		defaults.put(THREAD_POOL_DETAILED_DUMP, "false");
		defaults.put(STOP_TIMEOUT, "7000");
		defaults.put(INHERIT_CHANNEL, "false");
		defaults.put(WEBAPP, "src/main/webapp/");

		Names.bindProperties(binder(), defaults);
		Names.bindProperties(binder(), System.getProperties());
	}

	@Provides
	private Server provideServer(
			WebAppContext webAppContext,
			final ConnectionFactory connectionFactory,
			QueuedThreadPool queuedThreadPool,
			@Named(DUMP_BEFORE_STOP) boolean dumpBeforeStop,
			@Named(HOST) String host,
			@Named(PORT) int port,
			@Named(IDLE_TIMEOUT) int idleTimeOut,
			@Named(SO_LINGER_TIME) int soLingerTime,
			@Named(ACCEPTOR_PRIORITY_DELTA) int acceptorPriorityDelta,
			@Named(ACCEPT_QUERY_SIZE) int acceptQueueSize,
			@Named(STOP_TIMEOUT) int stopTimeout,
			@Named(INHERIT_CHANNEL) boolean inheritChannel
			) throws Exception {
		Server server = new Server(queuedThreadPool);

		final ServerConnector serverConnector = new ServerConnector(server);
		if (inheritChannel) {
			if (System.inheritedChannel() == null) {
				throw new IllegalStateException("There's no inherited channel.");
			}
			serverConnector.setInheritChannel(true);
		}
		serverConnector.setHost(host);
		serverConnector.setPort(port);
		serverConnector.setIdleTimeout(idleTimeOut);
		serverConnector.setSoLingerTime(soLingerTime);
		serverConnector.setAcceptorPriorityDelta(acceptorPriorityDelta);
		serverConnector.setConnectionFactories(Collections.singletonList(connectionFactory));
		serverConnector.setAcceptQueueSize(acceptQueueSize);
		server.addConnector(serverConnector);

		server.setDumpBeforeStop(dumpBeforeStop);
		server.setStopAtShutdown(true);
		server.setStopTimeout(stopTimeout);
		server.setHandler(webAppContext);

		// enable jmx
		MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		server.addEventListener(mbContainer);
		server.addBean(mbContainer);

		return server;
	}

	@Provides
	private WebAppContext provideWebAppContext(
			@Named(WEBAPP) String webApp
			) {
		if (!new File(webApp).exists()) {
			throw new RuntimeException("'" + WEBAPP + "' provides unknown path: " + webApp);
		}

		final WebAppContext webAppContext = new WebAppContext(webApp, "/");
		webAppContext.setErrorHandler(
			new ErrorHandler() {
				@Override
				public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
						final HttpServletResponse response)
						throws IOException {
					baseRequest.setHandled(true);
					response.setContentType("text/html; charset=utf-8");
					String cacheControl = "must-revalidate,no-cache,no-store";
					response.setHeader(HttpHeader.CACHE_CONTROL.asString(), cacheControl);
					log.info("ErrorHandler: {}", target);
					String message = HttpStatus.getMessage(response.getStatus());
					byte[] html = String.format("<!doctype html>%n<html><B>%d</B> %s</html>%n",
						response.getStatus(), message)
						.getBytes(StandardCharsets.UTF_8);
					response.setContentLength(html.length);
					response.getOutputStream().write(html);
				}
			}
			);
		webAppContext.setHandler(new StatisticsHandler());
		webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		return webAppContext;
	}

	@Provides
	private ConnectionFactory provideConnectionFactory(
			@NonNull HttpConfiguration httpConfiguration
			) {
		return new HttpConnectionFactory(httpConfiguration);
	}

	@Provides
	private HttpConfiguration provideHttpConfiguration() {
		// Enable X-Forwarded-For processing
		final HttpConfiguration httpConfiguration = new HttpConfiguration();
		httpConfiguration.addCustomizer(new ForwardedRequestCustomizer());
		return httpConfiguration;
	}

	@Provides
	private QueuedThreadPool provideQueuedThreadPool(
			@Named(THREAD_POOL_MAX_THREADS) int threadPoolMaxThreads,
			@Named(THREAD_POOL_MIN_THREADS) int threadPoolMinThreads,
			@Named(THREAD_POOL_DETAILED_DUMP) boolean threadPoolDetailedDump
			) {
		final QueuedThreadPool queuedThreadPool = new QueuedThreadPool();
		queuedThreadPool.setMaxThreads(threadPoolMaxThreads);
		queuedThreadPool.setMinThreads(threadPoolMinThreads);
		queuedThreadPool.setDetailedDump(threadPoolDetailedDump);
		return queuedThreadPool;
	}
}
