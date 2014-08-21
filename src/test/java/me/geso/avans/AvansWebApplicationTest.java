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

import lombok.Data;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;
import me.geso.testmech.TestMechResponse;
import me.geso.testmech.TestMechServlet;

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

		public static AvansResponse json(AvansWebApplication web) {
			Foo f = web.getRequest().readJSON(Foo.class);
			AvansAPIResponse<String> res = new AvansAPIResponse<>("name:" + f.name);
			return web.renderJSON(res);
		}
		
		public static AvansResponse mustache(AvansWebApplication web) {
			return web.renderMustache("mustache.mustache", new Foo());
		}

		@Data
		public static class Foo {
			String name = "John";
		}
	}

	@Test
	public void test() throws Exception {
		TestMechServlet mech = new TestMechServlet(MyServlet.class);

		{
			TestMechResponse res = mech.get("/").execute();
			assertEquals(200, res.getStatus());
			assertEquals("application/json; charset=utf-8",
					res.getContentType());
			assertEquals("{\"code\":200,\"messages\":[],\"data\":\"hoge\"}",
					res.getContentString());
		}

		{
			TestMechResponse res = mech.get("/intarg/5963").execute();
			assertEquals(200, res.getStatus());
			assertEquals("application/json; charset=utf-8",
					res.getContentType());
			assertEquals("{\"code\":200,\"messages\":[],\"data\":\"INTARG:5963\"}",
					res.getContentString());
		}

		{
			TestMechResponse res = mech.get("/longarg/5963").execute();
			assertEquals(200, res.getStatus());
			assertEquals("application/json; charset=utf-8",
					res.getContentType());
			assertEquals("{\"code\":200,\"messages\":[],\"data\":\"LONGARG:5963\"}",
					res.getContentString());
		}

		{
			TestMechResponse res = mech.get("/mustache").execute();
			assertEquals(200, res.getStatus());
			assertEquals("text/html; charset=UTF-8", res.getContentType());
			assertEquals("Hi, John!\n", res.getContentString());
		}

		{
			MyController.Foo foo = new MyController.Foo();
			foo.setName("iyan");
			TestMechResponse res = mech.postJSON("/json", foo).execute();
			assertEquals(200, res.getStatus());
			assertEquals("application/json; charset=utf-8", res.getContentType());
			assertEquals("{\"code\":200,\"messages\":[],\"data\":\"name:iyan\"}",
					res.getContentString());
		}

		{
			MyController.Foo foo = new MyController.Foo();
			foo.setName("iyan");
			TestMechResponse res = mech.postJSON("/json", foo).execute();
			assertEquals(200, res.getStatus());
			assertEquals("application/json; charset=utf-8", res.getContentType());
			@SuppressWarnings("unchecked")
			AvansAPIResponse<String> data = res.readJSON(AvansAPIResponse.class);
			assertEquals(data.code, 200);
			assertEquals(data.data, "name:iyan");
		}

		{
			TestMechResponse res = mech.get("/").execute();
			res.assertSuccess();
			res.assertContentTypeStartsWith("application/json");
			res.assertContentTypeContains("json");
			// res.contentTypeMatches("application/json");
			System.out.println(res.getContentString());
			res.assertContentContains("hoge");
		}
	}
}
