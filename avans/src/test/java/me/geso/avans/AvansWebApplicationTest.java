package me.geso.avans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Data;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;

import org.junit.After;
import org.junit.Before;
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
			router.post("/json", MyController::json);
			router.get("/cb", MyController::callback);
			router.get("/query", MyController::query);
			router.post("/postForm", MyController::postForm);
			router.post("/postMultipart", MyController::postMultipart);
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
			System.out.println(String.format("%s %s", method, path));
			RoutingResult<BasicAction> match = router.match(
					method, path);
			if (match == null) {
				return this.errorNotFound();
			}
			if (!match.methodAllowed()) {
				return this.errorMethodNotAllowed();
			}

			Map<String, String> captured = match.getCaptured();
			this.setPathParameters(captured);
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
		public Path getBaseDirectory() {
			return Paths.get(System.getProperty("user.dir"),
					"src/test/resources/");
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
			AvansAPIResponse<String> res = new AvansAPIResponse<>("INTARG:"
					+ web.getPathParameters().getInt("id"));
			return web.renderJSON(res);
		}

		public static AvansResponse longarg(AvansWebApplication web) {
			AvansAPIResponse<String> res = new AvansAPIResponse<>("LONGARG:"
					+ web.getPathParameters().getInt("id"));
			return web.renderJSON(res);
		}

		public static AvansResponse json(AvansWebApplication web) {
			Foo f = web.getRequest().readJSON(Foo.class);
			AvansAPIResponse<String> res = new AvansAPIResponse<>("name:"
					+ f.name);
			return web.renderJSON(res);
		}

		public static AvansResponse callback(AvansWebApplication web) {
			return new AvansCallbackResponse((resp) -> {
				resp.setContentType("text/plain; charset=utf-8");
				resp.getWriter().write("いぇーい");
			});
		}

		public static AvansResponse query(AvansWebApplication web) {
			String text = "name:" + web.getRequest().getParameter("name").get();
			return web.renderTEXT(text);
		}

		public static AvansResponse mustache(AvansWebApplication web) {
			return web.renderMustache("mustache.mustache", new Foo());
		}

		public static AvansResponse postForm(AvansWebApplication web) {
			String text = "(postform)name:"
					+ web.getRequest().getParameter("name").get();
			return web.renderTEXT(text);
		}

		public static AvansResponse postMultipart(AvansWebApplication web) {
			String text = "(postform)name:"
					+ web.getRequest().getParameter("name").get()
					+ ":"
					+ web.getRequest().getFileItem("tmpl").get().getString();
			return web.renderTEXT(text);
		}

		@Data
		public static class Foo {
			String name = "John";
		}
	}

	private MechJettyServlet mech;

	@Before
	public void before() {
		this.mech = new MechJettyServlet(MyServlet.class);
	}

	@After
	public void after() throws Exception {
		if (this.mech != null) {
			this.mech.close();
		}
	}

	@Test
	public void test() throws Exception {
		try (MechResponse res = mech.get("/").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(), "application/json");
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"hoge\"}");
		}

		try (MechResponse res = mech.get("/intarg/5963").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(), "application/json");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"INTARG:5963\"}");
		}

		try (MechResponse res = mech.get("/longarg/5963").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(), "application/json");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"LONGARG:5963\"}");
		}

		try (MechResponse res = mech.get("/mustache").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(), "text/html");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(res.getContentString(), "Hi, John!\n");
		}

		{
			MyController.Foo foo = new MyController.Foo();
			foo.setName("iyan");
			try (MechResponse res = mech.postJSON("/json", foo).execute()) {
				assertEquals(res.getStatusCode(), 200);
				assertEquals(res.getContentType().getMimeType(), "application/json");
				assertEquals(res.getContentType().getCharset().displayName(),
						"UTF-8");
				assertEquals(res.getContentString(),
						"{\"code\":200,\"messages\":[],\"data\":\"name:iyan\"}");
			}
		}

		{
			MyController.Foo foo = new MyController.Foo();
			foo.setName("iyan");
			try (MechResponse res = mech.postJSON("/json", foo).execute()) {
				assertEquals(res.getStatusCode(), 200);
				assertEquals(res.getContentType().getMimeType(),
						"application/json");
				assertEquals(res.getContentType().getCharset().displayName(),
						"UTF-8");

				@SuppressWarnings("unchecked")
				AvansAPIResponse<String> data = res
						.readJSON(AvansAPIResponse.class);
				assertEquals(data.code, 200);
				assertEquals(data.data, "name:iyan");
			}
		}

		{
			try (MechResponse res = mech.get("/").execute()) {
				assertEquals(res.getStatusCode(), 200);
				assertEquals(res.getContentType().getMimeType(),
						"application/json");
				assertEquals(res.getContentType().getCharset().displayName(),
						"UTF-8");
				assertTrue(res.getContentString().contains("hoge"));
			}
		}

		try (MechResponse res = mech.get("/cb").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(),
					"text/plain");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertTrue(res.getContentString().contains("いぇーい"));
		}

		try (MechResponse res = mech.get("/query?name=%E3%81%8A%E3%81%BB")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertTrue(res.getContentString().contains("name:おほ"));
		}

		try (
				MechResponse res = mech.post("/postForm")
						.param("name", "田中")
						.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertTrue(res.getContentString().contains("(postform)name:田中"));
		}
	}

	@Test
	public void testPostMultipart() throws Exception {
		try (MechResponse res = mech
				.postMultipart("/postMultipart")
				.param("name", "田中")
				.file("tmpl",
						new File("src/test/resources/tmpl/mustache.mustache"))
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertTrue(res.getContentString().contains("(postform)name:田中"));
		}
	}
}
