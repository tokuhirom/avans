package me.geso.avans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Data;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.QueryParam;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AvansWebApplicationTest {

	public static class MyServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;
		private static final Dispatcher dispatcher = new Dispatcher();
		static {
			dispatcher.registerClass(MyController.class);
		}

		public void service(ServletRequest req, ServletResponse resp)
				throws ServletException, IOException {
			dispatcher.handler((HttpServletRequest) req,
					(HttpServletResponse) resp);
		}
	}

	public static class MyControllerBase extends ControllerBase {

		@Override
		public Path getBaseDirectory() {
			return Paths.get(System.getProperty("user.dir"),
					"src/test/resources/");
		}
	}

	public static class MyController extends MyControllerBase {

		@GET("/")
		public WebResponse root() {
			APIResponse<String> res = new APIResponse<>("hoge");
			return this.renderJSON(res);
		}

		@GET("/intarg/{id}")
		public WebResponse intarg() {
			APIResponse<String> res = new APIResponse<>("INTARG:"
					+ this.getPathParameters().getInt("id"));
			return this.renderJSON(res);
		}

		@GET("/longarg/{id}")
		public WebResponse longarg() {
			APIResponse<String> res = new APIResponse<>("LONGARG:"
					+ this.getPathParameters().getInt("id"));
			return this.renderJSON(res);
		}

		@POST("/json")
		public WebResponse json() {
			Foo f = this.getRequest().readJSON(Foo.class);
			APIResponse<String> res = new APIResponse<>("name:"
					+ f.name);
			return this.renderJSON(res);
		}

		@POST("/jsonParam")
		public WebResponse jsonParam(@JsonParam Foo f) {
			APIResponse<String> res = new APIResponse<>("name:"
					+ f.name);
			return this.renderJSON(res);
		}

		@GET("/cb")
		public WebResponse callback() {
			return new CallbackResponse((resp) -> {
				resp.setContentType("text/plain; charset=utf-8");
				resp.getWriter().write("いぇーい");
			});
		}

		@GET("/query")
		public WebResponse query() {
			String text = "name:"
					+ this.getRequest().getQueryParams().get("name");
			return this.renderTEXT(text);
		}

		@GET("/mustache")
		public WebResponse mustache() {
			return this.renderMustache("mustache.mustache", new Foo());
		}

		@POST("/postForm")
		public WebResponse postForm() {
			String text = "(postform)name:"
					+ this.getRequest().getBodyParams().get("name");
			return this.renderTEXT(text);
		}

		@POST("/postMultipart")
		public WebResponse postMultipart() {
			String text = "(postform)name:"
					+ this.getRequest().getBodyParams().get("name")
					+ ":"
					+ this.getRequest().getFileItem("tmpl").get().getString();
			return this.renderTEXT(text);
		}

		@GET("/queryParamAnnotation")
		public WebResponse queryParamAnnotation(@QueryParam("a") String a) {
			String text = "a:" + a;
			return this.renderTEXT(text);
		}

		@GET("/pathParamAnnotation/{a}")
		public WebResponse pathParamAnnotation(@PathParam("a") String a) {
			String text = "a:" + a;
			return this.renderTEXT(text);
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
				APIResponse<String> data = res
						.readJSON(APIResponse.class);
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

	}

	@Test
	public void testMustache() {
		try (MechResponse res = mech.get("/mustache").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(), "text/html");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(res.getContentString(), "Hi, John!\n");
		}
	}

	@Test
	public void testPostForm() {
		try (
				MechResponse res = mech.post("/postForm")
						.param("name", "田中")
						.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertTrue(res.getContentString().contains("(postform)name:田中"));
		}
	}

	@Test
	public void testJson() {
		MyController.Foo foo = new MyController.Foo();
		foo.setName("iyan");
		try (MechResponse res = mech.postJSON("/json", foo).execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(),
					"application/json");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"name:iyan\"}");
		}
	}

	@Test
	public void testJsonParam() {
		MyController.Foo foo = new MyController.Foo();
		foo.setName("iyan");
		try (MechResponse res = mech.postJSON("/jsonParam", foo).execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(),
					"application/json");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"name:iyan\"}");
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

	@Test
	public void testQueryParamAnnotation() throws Exception {
		try (MechResponse res = mech
				.get("/queryParamAnnotation?a=b")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:b", res.getContentString());
		}
	}

	@Test
	public void testPathParamAnnotation() throws Exception {
		try (MechResponse res = mech
				.get("/pathParamAnnotation/b")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:b", res.getContentString());
		}
	}
}
