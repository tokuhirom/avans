package me.geso.servletmech;

import java.net.URI;

import javax.servlet.Servlet;

import lombok.SneakyThrows;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Alpha quaility... I will change API without notice. And so, I want to split
 * the distribution if it's okay.
 * 
 * @author tokuhirom
 *
 */
public class ServletMech {
	private Server server;
	private String baseURL;
	BasicCookieStore cookieStore = new BasicCookieStore();

	@SneakyThrows
	public ServletMech(Class<? extends Servlet> servlet) {
		this.server = createServer(servlet);
		this.server.start();
		ServerConnector connector = (ServerConnector) server
				.getConnectors()[0];
		int port = connector.getLocalPort();
		this.baseURL = "http://127.0.0.1:" + port;
	}

	public void addCookie(Cookie cookie) {
		this.cookieStore.addCookie(cookie);
	}

	@SneakyThrows
	public ServletMechRequest get(String path) {
		URI url = new URIBuilder(baseURL).setPath(path).build();
		HttpGet get = new HttpGet(url);
		return new ServletMechRequest(cookieStore, get);
	}

	@SneakyThrows
	public <T> ServletMechRequest postJSON(String path, T params) {
		if (params == null) {
			throw new RuntimeException("Params should not be null");
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
		byte[] json = mapper.writeValueAsBytes(params);
		URI url = new URIBuilder(baseURL).setPath(path).build();
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type",
				"application/json; charset=utf-8");
		post.setEntity(new ByteArrayEntity(json));
		return new ServletMechRequest(cookieStore, post);
	}

	private Server createServer(Class<? extends Servlet> servlet) {
		int port = 0;
		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(
				server,
				"/",
				ServletContextHandler.SESSIONS
				);
		context.addServlet(servlet, "/*");
		server.setStopAtShutdown(true);
		return server;
	}
}