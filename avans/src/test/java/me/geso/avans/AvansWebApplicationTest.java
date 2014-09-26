package me.geso.avans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Data;
import lombok.SneakyThrows;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.QueryParam;
import me.geso.avans.annotation.UploadFile;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.tinyvalidator.constraints.NotNull;
import me.geso.webscrew.request.WebRequestUpload;
import me.geso.webscrew.response.CallbackResponse;
import me.geso.webscrew.response.WebResponse;

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

		@Override
		public void service(ServletRequest req, ServletResponse resp)
				throws ServletException, IOException {
			dispatcher.handler((HttpServletRequest) req,
					(HttpServletResponse) resp);
		}
	}

	public static class MyController extends ControllerBase {

		@GET("/")
		public WebResponse root() {
			final APIResponse<String> res = new APIResponse<>("hoge");
			return this.renderJSON(res);
		}

		@GET("/intarg/{id}")
		public WebResponse intarg() {
			final APIResponse<String> res = new APIResponse<>("INTARG:"
					+ this.getPathParameters().getInt("id"));
			return this.renderJSON(res);
		}

		@GET("/longarg/{id}")
		public WebResponse longarg() {
			final APIResponse<String> res = new APIResponse<>("LONGARG:"
					+ this.getPathParameters().getInt("id"));
			return this.renderJSON(res);
		}

		@POST("/json")
		public WebResponse json() {
			final Foo f = this.getRequest().readJSON(Foo.class);
			final APIResponse<String> res = new APIResponse<>("name:"
					+ f.name);
			return this.renderJSON(res);
		}

		@GET("/jsonEasy")
		public APIResponse<String> jsonEasy() {
			final APIResponse<String> res = new APIResponse<>("It's easy!");
			return res;
		}

		@POST("/jsonParam")
		public WebResponse jsonParam(@JsonParam Foo f) {
			final APIResponse<String> res = new APIResponse<>("name:"
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
			final String text = "name:"
					+ this.getRequest().getQueryParams().get("name");
			return this.renderText(text);
		}

		@POST("/postForm")
		public WebResponse postForm() {
			final String text = "(postform)name:"
					+ this.getRequest().getBodyParams().get("name");
			return this.renderText(text);
		}

		@POST("/postMultipart")
		public WebResponse postMultipart() {
			final String text = "(postform)name:"
					+ this.getRequest().getBodyParams().get("name")
					+ ":"
					+ this.getRequest().getFileItem("tmpl").get().getString("UTF-8");
			return this.renderText(text);
		}

		@GET("/queryParamAnnotation")
		public WebResponse queryParamAnnotation(@QueryParam("a") String a,
				@QueryParam("b") OptionalInt b, @QueryParam("c") OptionalInt c) {
			final String text = "a:" + a + ",b:" + b + ",c:" + c;
			return this.renderText(text);
		}

		@GET("/pathParamAnnotation/{a}")
		public WebResponse pathParamAnnotation(@PathParam("a") String a) {
			final String text = "a:" + a;
			return this.renderText(text);
		}

		@GET("/optionalString")
		public WebResponse optionalString(@QueryParam("a") Optional<String> a) {
			final String text = "a:" + a;
			return this.renderText(text);
		}

		@POST("/uploadFile")
		@SneakyThrows
		public WebResponse uploadFile(@UploadFile("a") WebRequestUpload a) {
			final String text = "a:" + a.getString("UTF-8");
			return this.renderText(text);
		}

		@POST("/uploadOptionalFile")
		@SneakyThrows
		public WebResponse uploadOptionalFile(
				@UploadFile("a") Optional<WebRequestUpload> a) {
			String text = "a:";
			if (a.isPresent()) {
				text = text + a.get().getString("UTF-8");
			} else {
				text = text + "missing";
			}
			return this.renderText(text);
		}

		@POST("/uploadFileArray")
		@SneakyThrows
		public WebResponse uploadFileArray(@UploadFile("a") WebRequestUpload[] a) {
			final StringBuilder builder = new StringBuilder();
			builder.append("a:");
			for (final WebRequestUpload item : a) {
				builder.append(item.getString("UTF-8"));
				builder.append(",");
			}
			return this.renderText(builder.toString());
		}
	}

	@Data
	public static class Foo {
		@NotNull
		private String name;
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
		try (MechResponse res = this.mech.get("/").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(), "application/json");
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"hoge\"}");
		}

		try (MechResponse res = this.mech.get("/intarg/5963").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(), "application/json");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"INTARG:5963\"}");
		}

		try (MechResponse res = this.mech.get("/longarg/5963").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(), "application/json");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"LONGARG:5963\"}");
		}

		{
			final Foo foo = new Foo();
			foo.setName("iyan");
			try (MechResponse res = this.mech.postJSON("/json", foo).execute()) {
				assertEquals(res.getStatusCode(), 200);
				assertEquals(res.getContentType().getMimeType(),
						"application/json");
				assertEquals(res.getContentType().getCharset().displayName(),
						"UTF-8");

				@SuppressWarnings("unchecked")
				final
				APIResponse<String> data = res
						.readJSON(APIResponse.class);
				assertEquals(data.code, 200);
				assertEquals(data.data, "name:iyan");
			}
		}

		{
			try (MechResponse res = this.mech.get("/").execute()) {
				assertEquals(res.getStatusCode(), 200);
				assertEquals(res.getContentType().getMimeType(),
						"application/json");
				assertEquals(res.getContentType().getCharset().displayName(),
						"UTF-8");
				assertTrue(res.getContentString().contains("hoge"));
			}
		}

		try (MechResponse res = this.mech.get("/cb").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentType().getMimeType(),
					"text/plain");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertTrue(res.getContentString().contains("いぇーい"));
		}

		try (MechResponse res = this.mech.get("/query?name=%E3%81%8A%E3%81%BB")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertTrue(res.getContentString().contains("name:おほ"));
		}

	}

	@Test
	public void testPostForm() {
		try (
				MechResponse res = this.mech.post("/postForm")
						.param("name", "田中")
						.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertTrue(res.getContentString().contains("(postform)name:田中"));
		}
	}

	@Test
	public void testJson() {
		final Foo foo = new Foo();
		foo.setName("iyan");
		try (MechResponse res = this.mech.postJSON("/json", foo).execute()) {
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
	public void testJsonEasy() {
		try (MechResponse res = this.mech.get("/jsonEasy").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"It's easy!\"}");
		}
	}

	@Test
	public void testJsonParam() {
		System.setProperty("org.jboss.logging.provider", "slf4j");
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		{
			final Foo foo = new Foo();
			foo.setName("iyan");
			try (MechResponse res = this.mech.postJSON("/jsonParam", foo).execute()) {
				assertEquals(res.getStatusCode(), 200);
				assertEquals(res.getContentType().getMimeType(),
						"application/json");
				assertEquals(res.getContentType().getCharset().displayName(),
						"UTF-8");
				assertEquals(res.getContentString(),
						"{\"code\":200,\"messages\":[],\"data\":\"name:iyan\"}");
			}
		}

	}

	@Test
	public void testJsonParamValidationFailed() {
		// validation failed.
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		final Foo foo = new Foo();
		foo.setName(null);
		try (MechResponse res = this.mech.postJSON("/jsonParam", foo).execute()) {
			assertEquals(200, res.getStatusCode());
			assertEquals(res.getContentType().getMimeType(),
					"application/json");
			assertEquals(res.getContentType().getCharset().displayName(),
					"UTF-8");
			assertEquals(
					"{\"code\":403,\"messages\":[\"name may not be null.\"],\"data\":null}",
					res.getContentString());
		}
	}

	@Test
	public void testPostMultipart() throws Exception {
		try (MechResponse res = this.mech
				.postMultipart("/postMultipart")
				.param("name", "田中")
				.file("tmpl",
						new File(
								"src/test/resources/hello.txt"))
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertTrue(res.getContentString().contains("(postform)name:田中"));
		}
	}

	@Test
	public void testQueryParamAnnotation() throws Exception {
		try (MechResponse res = this.mech
				.get("/queryParamAnnotation?a=b")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:b,b:OptionalInt.empty,c:OptionalInt.empty",
					res.getContentString());
		}
		try (MechResponse res = this.mech
				.get("/queryParamAnnotation?a=b&b=4&c=5")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:b,b:OptionalInt[4],c:OptionalInt[5]",
					res.getContentString());
		}
	}

	@Test
	public void testPathParamAnnotation() throws Exception {
		try (MechResponse res = this.mech
				.get("/pathParamAnnotation/b")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:b", res.getContentString());
		}
	}

	@Test
	public void testOptionalString() throws Exception {
		try (MechResponse res = this.mech
				.get("/optionalString?a=b")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:Optional[b]", res.getContentString());
		}

		try (MechResponse res = this.mech
				.get("/optionalString")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:Optional.empty", res.getContentString());
		}
	}

	@Test
	public void testUploadFile() throws Exception {
		try (MechResponse res = this.mech
				.postMultipart("/uploadFile")
				.file("a", new File("src/test/resources/hello.txt"))
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:hello", res.getContentString());
		}
	}

	@Test
	public void testUploadOptionalFile() throws Exception {
		try (MechResponse res = this.mech
				.postMultipart("/uploadOptionalFile")
				.file("a", new File("src/test/resources/hello.txt"))
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:hello", res.getContentString());
		}

		// missing
		try (MechResponse res = this.mech
				.postMultipart("/uploadOptionalFile")
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:missing", res.getContentString());
		}
	}

	@Test
	public void testUploadFileArray() throws Exception {
		try (MechResponse res = this.mech
				.postMultipart("/uploadFileArray")
				.file("a", new File("src/test/resources/hello.txt"))
				.file("a", new File("src/test/resources/hello.txt"))
				.execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals("a:hello,hello,", res.getContentString());
		}
	}
}
