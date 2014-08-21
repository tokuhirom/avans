package me.geso.avans;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.routes.WebRouter;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

public class AvansWebApplicationTest {

	public static class MyServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		public void service(ServletRequest req, ServletResponse res)
				throws ServletException, IOException {
			MyApplication app = new MyApplication((HttpServletRequest) req,
					(HttpServletResponse) res);
			app.run();
		}
	}

	public static class MyApplication extends AvansWebApplication {
		public MyApplication(HttpServletRequest servletRequest,
				HttpServletResponse servletResponse) throws IOException {
			super(servletRequest, servletResponse);
		}

		@Override
		public WebRouter<AvansAction> getRouter() {
			WebRouter<AvansAction> routes = new WebRouter<>();
			routes.get("/", MyController::root);
			return routes;
		}
	}

	public static class MyController {
		public static AvansResponse root(AvansWebApplication web,
				Map<String, String> params) {
			AvansAPIResponse<String> res = new AvansAPIResponse<>("hoge");
			return web.renderJSON(res);
		}
	}

	public Server createServer(int port) {
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

	@Test
	public void test() throws Exception {
		Server server = createServer(0);
		server.start();
		ServerConnector connector = (ServerConnector)server.getConnectors()[0];
		int port = connector.getLocalPort();
		
		Response response = Request.Get("http://localhost:" + port + "/").execute();
		assertEquals(
				response.returnContent().asString(),
				"{\"code\":200,\"messages\":[],\"data\":\"hoge\"}");

	}
}
