package me.geso.avans;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.SneakyThrows;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

public class AvansWebApplicationTest {

	public static class MyServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		public void service(ServletRequest req, ServletResponse res)
				throws ServletException, IOException {
			try (MyApplication app = new MyApplication(
					(HttpServletRequest) req,
					(HttpServletResponse) res)) {
				app.run();
			}
		}
	}

	@FunctionalInterface
	public interface BasicAction {
		public AvansResponse run(AvansWebApplication web);
	}

	public static class MyApplication extends AvansWebApplication {
		static WebRouter<BasicAction> router;
		static {
			router = new WebRouter<>();
			router.get("/", MyController::root);
			router.get("/mustache", MyController::mustache);
			router.get("/intarg/{id}", MyController::intarg);
			router.get("/longarg/{id}", MyController::longarg);
		}

		public MyApplication(HttpServletRequest servletRequest,
				HttpServletResponse servletResponse) throws IOException {
			super(servletRequest, servletResponse);
		}

		@Override
		public AvansResponse dispatch() {
			AvansRequest request = this.getRequest();
			String method = getRequest().getMethod();
			String path = getRequest().getPathInfo();
			RoutingResult<BasicAction> match = router.match(
					method, path);
			if (match == null) {
				return this.errorNotFound();
			}
			if (!match.methodAllowed()) {
				return this.errorMethodNotAllowed();
			}

			Map<String, String> captured = match.getCaptured();
			this.setArgs(captured);
			BasicAction destination = match.getDestination();
			AvansResponse response = destination.run(this);
			if (response == null) {
				throw new RuntimeException(String.format(
						"Response must not be null: %s, %s, %s",
						request.getMethod(), request.getPathInfo(),
						destination.toString()
						));
			}
			return response;
		}

		@Override
		public String getBaseDirectory() {
			return System.getProperty("user.dir") + "/src/test/resources/";
		}

		@Override
		public void close() throws IOException {
		}
	}

	public static class MyController {
		public static AvansResponse root(AvansWebApplication web) {
			AvansAPIResponse<String> res = new AvansAPIResponse<>("hoge");
			return web.renderJSON(res);
		}

		public static AvansResponse intarg(AvansWebApplication web) {
			AvansAPIResponse<String> res = new AvansAPIResponse<>("INTARG:" + web.getIntArg("id"));
			return web.renderJSON(res);
		}

		public static AvansResponse longarg(AvansWebApplication web) {
			AvansAPIResponse<String> res = new AvansAPIResponse<>("LONGARG:" + web.getLongArg("id"));
			return web.renderJSON(res);
		}

		public static AvansResponse mustache(AvansWebApplication web) {
			return web.renderMustache("mustache.mustache", new Foo());
		}

		static class Foo {
			String name = "John";
		}
	}

	@Test
	public void test() throws Exception {
		ServletMech mech = new ServletMech(new MyServlet());
		{
			ServletMechResponse res = mech.get("/");
			assertEquals(200, res.getStatus());
			assertEquals("application/json; charset=utf-8",
					res.getContentType());
			assertEquals("{\"code\":200,\"messages\":[],\"data\":\"hoge\"}",
					res.getBodyString());
		}

		{
			ServletMechResponse res = mech.get("/intarg/5963");
			assertEquals(200, res.getStatus());
			assertEquals("application/json; charset=utf-8",
					res.getContentType());
			assertEquals("{\"code\":200,\"messages\":[],\"data\":\"INTARG:5963\"}",
					res.getBodyString());
		}

		{
			ServletMechResponse res = mech.get("/longarg/5963");
			assertEquals(200, res.getStatus());
			assertEquals("application/json; charset=utf-8",
					res.getContentType());
			assertEquals("{\"code\":200,\"messages\":[],\"data\":\"LONGARG:5963\"}",
					res.getBodyString());
		}

		{
			ServletMechResponse res = mech.get("/mustache");
			assertEquals(200, res.getStatus());
			assertEquals("text/html; charset=UTF-8", res.getContentType());
			assertEquals("Hi, John!\n", res.getBodyString());
		}
	}

	public static class ServletMech {
		private Server server;
		private String baseURL;
		BasicCookieStore cookieStore = new BasicCookieStore();

		@SneakyThrows
		public ServletMech(HttpServlet servlet) {
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

		private Server createServer(HttpServlet servlet) {
			int port = 0;
			Server server = new Server(port);
			ServletContextHandler context = new ServletContextHandler(
					server,
					"/",
					ServletContextHandler.SESSIONS
					);
			context.addServlet(MyServlet.class, "/*");
			server.setStopAtShutdown(true);
			return server;
		}
	}

	public static class ServletMechResponse {

		@Getter
		private CloseableHttpResponse response;
		@Getter
		private byte[] body;

		public ServletMechResponse(CloseableHttpResponse response,
				byte[] body) {
			this.response = response;
			this.body = body;
		}

		public int getStatus() {
			return response.getStatusLine().getStatusCode();
		}

		public String getContentType() {
			return response.getFirstHeader("Content-Type").getValue();
		}

		public String getBodyString() {
			return new String(body, Charset.forName("UTF-8"));
		}
	}
}
