package me.geso.avans;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.geso.avans.annotation.BeanParam;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.UploadFile;
import me.geso.avans.trigger.ResponseConverter;
import me.geso.mech.Mech;
import me.geso.mech.MechResponse;
import me.geso.tinyvalidator.constraints.NotNull;
import me.geso.webscrew.response.CallbackResponse;
import me.geso.webscrew.response.WebResponse;

public class AvansWebApplicationTest {

	private Mech mech;
	private Server server;

	@Before
	public void before() throws Exception {
		final ServletHolder servletHolder = new ServletHolder(MyServlet.class);
		final String tmpDirName = System.getProperty("java.io.tmpdir");
		servletHolder.getRegistration().setMultipartConfig(
			new MultipartConfigElement(tmpDirName));
		this.server = new Server(0);
		final ServletContextHandler context = new ServletContextHandler(
			this.server,
			"/",
			ServletContextHandler.SESSIONS
				);
		context.addServlet(servletHolder, "/*");
		this.server.setStopAtShutdown(true);
		this.server.start();
		final ServerConnector connector = (ServerConnector)this.server
			.getConnectors()[0];
		final int port = connector.getLocalPort();
		final String baseURL = "http://127.0.0.1:" + port;
		this.mech = new Mech(baseURL);
	}

	@After
	public void after() throws Exception {
		if (this.server != null) {
			this.server.stop();
		}
	}

	@Test
	public void test() throws Exception {
		try (MechResponse res = this.mech.get("/").execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals(res.getContentType().getMimeType(),
				"application/json");
			Assert.assertEquals(res.getContentString(),
				"{\"code\":200,\"messages\":[],\"data\":\"hoge\"}");
		}

		{
			try (MechResponse res = this.mech.get("/").execute()) {
				Assert.assertEquals(res.getStatusCode(), 200);
				Assert.assertEquals(res.getContentType().getMimeType(),
					"application/json");
				Assert.assertEquals(res.getContentType().getCharset()
					.displayName(),
					"UTF-8");
				Assert.assertTrue(res.getContentString().contains("hoge"));
			}
		}

		try (MechResponse res = this.mech.get("/cb").execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals(res.getContentType().getMimeType(),
				"text/plain");
			Assert.assertEquals(
				res.getContentType().getCharset().displayName(),
				"UTF-8");
			Assert.assertTrue(res.getContentString().contains("いぇーい"));
		}

		try (MechResponse res = this.mech.get("/query?name=%E3%81%8A%E3%81%BB")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertTrue(res.getContentString().contains("name:おほ"));
		}

		try (MechResponse res = this.mech.get("/query")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 400);
			System.out.println(res.getContentString());
			Assert.assertEquals(
				"{\"code\":400,\"messages\":[\"Missing mandatory parameter: name\"]}",
				res.getContentString());
		}

	}

	@Test
	public void testNg() throws IOException {

		try (MechResponse res = this.mech.get(
			"/ng/param/get?name=%E3%81%8A%E3%81%BB")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertTrue(res.getContentString().contains("name:おほ"));
		}

		try (MechResponse res = this.mech.post(
			"/ng/param/post")
			.param("name", "保坂")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertTrue(res.getContentString().contains("name:保坂"));
		}

		try (MechResponse res = this.mech.post(
			"/ng/param/post")
			.param("name", "保坂")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertTrue(res.getContentString().contains("name:保坂"));
		}

		try (MechResponse res = this.mech.postMultipart(
			"/ng/part/single")
			.param("p", "hey")
			.file("file", new File("src/test/resources/hello.txt"))
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			System.out.println(res.getContentString());
			Assert.assertEquals("p: heyfile:hello", res.getContentString());
		}

		try (MechResponse res = this.mech.postMultipart(
			"/ng/part/array")
			.file("file", new File("src/test/resources/hello.txt"))
			.file("file", new File("src/test/resources/hello.txt"))
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			System.out.println(res.getContentString());
			Assert.assertTrue(res.getContentString().contains("file:hello"));
		}
	}

	@Test
	public void testPostForm() throws IOException {
		try (
				MechResponse res = this.mech.post("/postForm")
					.param("name", "田中")
					.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertTrue(res.getContentString().contains(
				"(postform)name:田中"));
		}
	}

	@Test
	public void testJson() throws IOException {
		final Foo foo = new Foo();
		foo.setName("iyan");
	}

	@Test
	public void testJsonEasy() throws IOException {
		try (MechResponse res = this.mech.get("/jsonEasy").execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals(res.getContentString(),
				"{\"code\":200,\"messages\":[],\"data\":\"It's easy!\"}");
		}
	}

	@Test
	public void testJsonParam() throws IOException {
		System.setProperty("org.jboss.logging.provider", "slf4j");
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		{
			final Foo foo = new Foo();
			foo.setName("iyan");
			try (MechResponse res = this.mech.postJSON("/jsonParam", foo)
				.execute()) {
				Assert.assertEquals(res.getStatusCode(), 200);
				Assert.assertEquals(res.getContentType().getMimeType(),
					"application/json");
				Assert.assertEquals(res.getContentType().getCharset()
					.displayName(),
					"UTF-8");
				Assert.assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"name:iyan\"}");
			}
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
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertTrue(res.getContentString().contains(
				"(postform)name:田中"));
		}
	}

	@Test
	public void testParamAnnotation() throws Exception {
		try (MechResponse res = this.mech
			.get("/queryParamAnnotation?a=b")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:b,b:OptionalInt.empty,c:OptionalInt.empty",
				res.getContentString());
		}
		try (MechResponse res = this.mech
			.get("/queryParamAnnotation?a=b&b=4&c=5")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:b,b:OptionalInt[4],c:OptionalInt[5]",
				res.getContentString());
		}
	}

	@Test
	public void testPathParamAnnotation() throws Exception {
		try (MechResponse res = this.mech
			.get("/pathParamAnnotation/b")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:b", res.getContentString());
		}
	}

	@Test
	public void testOptionalString() throws Exception {
		try (MechResponse res = this.mech
			.get("/optionalString?a=b")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:Optional[b]", res.getContentString());
		}

		try (MechResponse res = this.mech
			.get("/optionalString")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:Optional.empty", res.getContentString());
		}
	}

	@Test
	public void testUploadFile() throws Exception {
		try (MechResponse res = this.mech
			.postMultipart("/uploadFile")
			.file("a", new File("src/test/resources/hello.txt"))
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:hello", res.getContentString());
		}
	}

	@Test
	public void testUploadOptionalFile() throws Exception {
		try (MechResponse res = this.mech
			.postMultipart("/uploadOptionalFile")
			.file("a", new File("src/test/resources/hello.txt"))
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:hello", res.getContentString());
		}

		// missing
		try (MechResponse res = this.mech
			.postMultipart("/uploadOptionalFile")
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:missing", res.getContentString());
		}
	}

	@Test
	public void testUploadFileArray() throws Exception {
		try (MechResponse res = this.mech
			.postMultipart("/uploadFileArray")
			.file("a", new File("src/test/resources/hello.txt"))
			.file("a", new File("src/test/resources/hello.txt"))
			.execute()) {
			Assert.assertEquals(res.getStatusCode(), 200);
			Assert.assertEquals("a:hello,hello,", res.getContentString());
		}
	}

	public static class MyServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;
		private static final Dispatcher dispatcher = new Dispatcher();
		static {
			MyServlet.dispatcher.registerClass(MyController.class);
		}

		@Override
		public void service(final ServletRequest req, final ServletResponse resp)
				throws ServletException, IOException {
			MyServlet.dispatcher.handler((HttpServletRequest)req,
				(HttpServletResponse)resp);
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class StringAPIResponse extends BasicAPIResponse {
		private String data;

		public StringAPIResponse(String data) {
			this.data = data;
		}
	}

	public static class MyController extends ControllerBase {

		@ResponseConverter(StringAPIResponse.class)
		public Optional<WebResponse> responseFilter(StringAPIResponse o) {
			return Optional.of(this.renderJSON(o));
		}

		@GET("/")
		public WebResponse root() {
			final StringAPIResponse res = new StringAPIResponse("hoge");
			return this.renderJSON(res);
		}

		@GET("/jsonEasy")
		public StringAPIResponse jsonEasy() {
			return new StringAPIResponse("It's easy!");
		}

		@POST("/jsonParam")
		public WebResponse jsonParam(@JsonParam final Foo f) {
			final StringAPIResponse res = new StringAPIResponse("name:"
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
		public WebResponse query(@Param("name") String name) {
			final String text = "name:" + name;
			return this.renderText(text);
		}

		@GET("/ng/param/get")
		public WebResponse paramGet(@Param("name") String name) {
			final String text = "name:" + name;
			return this.renderText(text);
		}

		@POST("/ng/param/post")
		public WebResponse paramPost(@Param("name") String name) {
			final String text = "name:" + name;
			return this.renderText(text);
		}

		@POST("/ng/part/single")
		public WebResponse partSingle(@NonNull @Param("p") String p,
				@NonNull @UploadFile("file") Part file)
				throws IOException {
			final String text = "p: " + p + "file:"
				+ IOUtils.toString(file.getInputStream(), "UTF-8");
			return this.renderText(text);
		}

		@POST("/ng/part/array")
		public WebResponse partArray(@UploadFile("file") Part[] files)
				throws IOException {
			final String text = "file:"
				+
				Arrays.stream(files)
					.map(it -> {
						try {
							return IOUtils.toString(
								it.getInputStream(),
								"UTF-8");
						} catch (final Exception e) {
							throw new RuntimeException(e);
						}
					})
					.collect(Collectors.joining(","));
			return this.renderText(text);
		}

		@POST("/postForm")
		public WebResponse postForm(@Param("name") String name) {
			final String text = "(postform)name:" + name;
			return this.renderText(text);
		}

		@POST("/postMultipart")
		public WebResponse postMultipart(@Param("name") String name,
				@UploadFile("tmpl") Part tmpl) throws IOException {
			final String text = "(postform)name:"
				+ name
				+ ":"
				+ IOUtils.toString(tmpl.getInputStream(), "UTF-8");
			return this.renderText(text);
		}

		@GET("/queryParamAnnotation")
		public WebResponse queryParamAnnotation(
				@Param("a") final String a,
				@Param("b") final OptionalInt b,
				@Param("c") final OptionalInt c) {
			final String text = "a:" + a + ",b:" + b + ",c:" + c;
			return this.renderText(text);
		}

		@GET("/pathParamAnnotation/{a}")
		public WebResponse pathParamAnnotation(@PathParam("a") final String a) {
			final String text = "a:" + a;
			return this.renderText(text);
		}

		@GET("/optionalString")
		public WebResponse optionalString(
				@Param("a") final Optional<String> a) {
			final String text = "a:" + a;
			return this.renderText(text);
		}

		@POST("/uploadFile")
		@SneakyThrows
		public WebResponse uploadFile(@UploadFile("a") final Part a) {
			final String text = "a:"
				+ IOUtils.toString(a.getInputStream(), "UTF-8");
			return this.renderText(text);
		}

		@POST("/uploadOptionalFile")
		@SneakyThrows
		public WebResponse uploadOptionalFile(
				@UploadFile("a") final Optional<Part> a) {
			String text = "a:";
			if (a.isPresent()) {
				text = text
					+ IOUtils.toString(a.get().getInputStream(), "UTF-8");
			} else {
				text = text + "missing";
			}
			return this.renderText(text);
		}

		@POST("/uploadFileArray")
		@SneakyThrows
		public WebResponse uploadFileArray(
				@UploadFile("a") final Part[] a) {
			final StringBuilder builder = new StringBuilder();
			builder.append("a:");
			for (final Part item : a) {
				builder.append(IOUtils.toString(item.getInputStream(), "UTF-8"));
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
}
