package me.geso.servletmech;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import javax.servlet.Servlet;

import lombok.SneakyThrows;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Alpha quaility... I will change API without notice. And so, I want to split the distribution if it's okay.
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

	@SneakyThrows
	public ServletMechResponse get(String path) {
		URI url = new URIBuilder(baseURL).setPath(path).build();
		{
			try (CloseableHttpClient httpclient = HttpClients.custom()
					.setDefaultCookieStore(cookieStore)
					.build()) {
				HttpGet get = new HttpGet(url);
				try (CloseableHttpResponse response = httpclient
						.execute(get)) {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					response.getEntity().writeTo(stream);
					byte[] byteArray = stream.toByteArray();
					return new ServletMechResponse(response, byteArray);
				}
			}
		}
	}

	@SneakyThrows
	public <T> ServletMechResponse postJSON(String path, T params) {
		if (params == null) {
			throw new RuntimeException("Params should not be null");
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
		byte[] json = mapper.writeValueAsBytes(params);
		URI url = new URIBuilder(baseURL).setPath(path).build();
		{
			try (CloseableHttpClient httpclient = HttpClients.custom()
					.setDefaultCookieStore(cookieStore)
					.build()) {
				HttpPost post = new HttpPost(url);
				post.setHeader("Content-Type", "application/json; charset=utf-8");
				post.setEntity(new ByteArrayEntity(json));
				try (CloseableHttpResponse response = httpclient
						.execute(post)) {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					response.getEntity().writeTo(stream);
					byte[] byteArray = stream.toByteArray();
					return new ServletMechResponse(response, byteArray);
				}
			}
		}
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