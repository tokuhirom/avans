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
import me.geso.testmech.TestMechJettyServlet;
import me.geso.testmech.TestMechResponse;

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
			AvansAPIResponse<String> res = new AvansAPIResponse<>("INTARG:"
					+ web.getIntArg("id"));
			return web.renderJSON(res);
		}

		public static AvansResponse longarg(AvansWebApplication web) {
			AvansAPIResponse<String> res = new AvansAPIResponse<>("LONGARG:"
					+ web.getLongArg("id"));
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
			String text = "name:" + web.getRequest().getParameter("name");
			return web.renderTEXT(text);
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
		TestMechJettyServlet mech = new TestMechJettyServlet(MyServlet.class);

		{
			TestMechResponse res = mech.get("/").execute();
			res.assertSuccess();
			res.assertContentTypeContains("json");
			res.assertContentEquals("{\"code\":200,\"messages\":[],\"data\":\"hoge\"}");
		}

		{
			TestMechResponse res = mech.get("/intarg/5963").execute();
			res.assertSuccess();
			res.assertContentTypeEquals("application/json; charset=utf-8");
			res.assertContentEquals(
					"{\"code\":200,\"messages\":[],\"data\":\"INTARG:5963\"}"
					);
		}

		{
			TestMechResponse res = mech.get("/longarg/5963").execute();
			res.assertSuccess();
			res.assertContentTypeEquals("application/json; charset=utf-8");
			res.assertContentEquals(
					"{\"code\":200,\"messages\":[],\"data\":\"LONGARG:5963\"}"
					);
		}

		{
			TestMechResponse res = mech.get("/mustache").execute();
			res.assertSuccess();
			res.assertContentTypeEquals("text/html; charset=UTF-8");
			res.assertContentEquals("Hi, John!\n");
		}

		{
			MyController.Foo foo = new MyController.Foo();
			foo.setName("iyan");
			TestMechResponse res = mech.postJSON("/json", foo).execute();
			res.assertSuccess();
			res.assertContentTypeEquals("application/json; charset=utf-8");
			res.assertContentEquals("{\"code\":200,\"messages\":[],\"data\":\"name:iyan\"}");
		}

		{
			MyController.Foo foo = new MyController.Foo();
			foo.setName("iyan");
			TestMechResponse res = mech.postJSON("/json", foo).execute();
			res.assertSuccess();
			res.assertContentTypeEquals("application/json; charset=utf-8");

			@SuppressWarnings("unchecked")
			AvansAPIResponse<String> data = res
					.readJSON(AvansAPIResponse.class);
			assertEquals(data.code, 200);
			assertEquals(data.data, "name:iyan");
		}

		{
			TestMechResponse res = mech.get("/").execute();
			res.assertSuccess();
			res.assertContentTypeStartsWith("application/json");
			res.assertContentTypeContains("json");
			res.assertContentContains("hoge");
		}

		{
			TestMechResponse res = mech.get("/cb").execute();
			res.assertSuccess();
			res.assertContentTypeEquals("text/plain; charset=UTF-8");
			res.assertContentContains("いぇーい");
		}

		{
			TestMechResponse res = mech.get("/query?name=%E3%81%8A%E3%81%BB")
					.execute();
			res.assertSuccess();
			res.assertContentContains("name:おほ");
		}
	}
}
