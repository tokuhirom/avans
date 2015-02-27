package me.geso.avans.annotation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.webscrew.response.WebResponse;

@Slf4j
public class BeanParamTest {

	@Test
	public void test() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		JettyServletTester
			.runServlet(
				servlet,
				(uri) -> {
					final Mech2WithBase mech2 = new Mech2WithBase(Mech2
						.builder()
						.build(), uri);
					{
						final Mech2Result res = mech2
							.get("/")
							.addQueryParameter("string", "a")
							.addQueryParameter("primitive_long",
								"1")
							.addQueryParameter("object_long", "2")
							.addQueryParameter("primitive_short",
								"3")
							.addQueryParameter("object_short", "4")
							.addQueryParameter("primitive_int", "5")
							.addQueryParameter("object_int", "6")
							.addQueryParameter("primitive_boolean",
								"false")
							.addQueryParameter("object_boolean",
								"true")
							.execute();
						log.info("response: {}",
							res.getResponseBodyAsString());
						assertEquals(200, res.getResponse()
							.getStatusLine()
							.getStatusCode());
						assertEquals(
							"{\"string\":\"a\",\"primitive_long\":1,\"object_long\":2,\"object_short\":4,\"primitive_short\":3,\"object_int\":6,\"primitive_int\":5,\"primitive_boolean\":false,\"object_boolean\":true}",
							res.getResponseBodyAsString());
					}

					{
						// no optional values
						final Mech2Result res = mech2
							.get("/oBean")
							.execute();
						log.info("response: {}",
							res.getResponseBodyAsString());
						assertEquals(200, res.getResponse()
							.getStatusLine()
							.getStatusCode());
						assertEquals(
							"{\"int\":0,\"long\":0,\"string\":\"-\"}",
							res.getResponseBodyAsString());
					}

					{
						// provide optional values
						final Mech2Result res = mech2
							.get("/oBean")
							.addQueryParameter("int", "5963")
							.addQueryParameter("long", "4949")
							.addQueryParameter("string", "hello")
							.execute();
						log.info("response: {}",
							res.getResponseBodyAsString());
						assertEquals(200, res.getResponse()
							.getStatusLine()
							.getStatusCode());
						assertEquals(
							"{\"int\":0,\"long\":0,\"string\":\"-\"}",
							res.getResponseBodyAsString());
					}
				});
	}

	@Test
	public void testFileBean() throws Exception {
		// all
		this.test(mech2 -> {
			// fileBean
			final Mech2Result res = mech2
				.postMultipart("/fileBean")
					.addTextBody("string", "SSS")
					.addBinaryBody("part", new File("src/test/resources/hello.txt"))
					.addBinaryBody("parts", new File("src/test/resources/hello.txt"))
					.addBinaryBody("parts", new File("src/test/resources/hello.txt"))
					.execute();
			log.info("response: {}",
					res.getResponseBodyAsString());
			assertEquals(200, res.getResponse()
					.getStatusLine()
					.getStatusCode());
			assertEquals(
					"{\"string\":\"SSS\",\"part\":\"hello\",\"parts\":\"hello,hello\"}",
					res.getResponseBodyAsString());
		});
	}

	private void test(TestCallback cb) throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		final ServletHolder servletHolder = new ServletHolder(servlet);
		final String tmpDirName = System.getProperty("java.io.tmpdir");
		servletHolder.getRegistration().setMultipartConfig(
			new MultipartConfigElement(tmpDirName));
		JettyServletTester
			.runServlet(
					servletHolder,
					(uri) -> {
						final Mech2WithBase mech2 = new Mech2WithBase(Mech2
								.builder()
								.build(), uri);
						cb.call(mech2);
					});
	}

	@FunctionalInterface
	private interface TestCallback {
		public void call(Mech2WithBase mech) throws IOException, URISyntaxException;
	}

	public static class MyController extends ControllerBase {
		@GET("/")
		public WebResponse foo(@BeanParam MyBean bean) {
			return this.renderJSON(bean);
		}

		@GET("/oBean")
		public WebResponse oBean(@BeanParam OBean bean) {
			return this.renderJSON(
				ImmutableMap.builder()
					.put("int", bean.getOptional_int().orElse(0))
					.put("long", bean.getOptional_long().orElse(0))
					.put("string",
						bean.getOptional_string().orElse("-"))
					.build());
		}

		@POST("/fileBean")
		public WebResponse fileBean(@BeanParam FileBean bean) throws IOException {
			return this.renderJSON(
				ImmutableMap.builder()
					.put("string", bean.getString())
					.put("part", IOUtils.toString(bean.getPart().getInputStream()))
					.put("parts",
						Arrays.stream(bean.getParts())
							.map(part -> {
								try {
									return IOUtils.toString(part.getInputStream());
								} catch (final IOException e) {
									throw new RuntimeException(e);
								}
							})
							.collect(Collectors.joining(","))
					)
					.build());
		}

		@Data
		public static class MyBean {
			@Param("string")
			private String string;

			@Param("primitive_long")
			private long primitive_long;

			@Param("object_long")
			private Long object_long;

			@Param("object_short")
			private Short object_short;

			@Param("primitive_short")
			private short primitive_short;

			@Param("object_int")
			private Integer object_int;

			@Param("primitive_int")
			private int primitive_int;

			@Param("primitive_boolean")
			private boolean primitive_boolean;

			@Param("object_boolean")
			private Boolean object_boolean;
		}

		@Data
		public static class OBean {
			@Param("optional_int")
			private OptionalInt optional_int;

			@Param("optional_long")
			private OptionalLong optional_long;

			@Param("optional_string")
			private Optional<String> optional_string;
		}

		@Data
		public static class FileBean {
			@Param("string")
			private String string;

			@UploadFile("part")
			private Part part;

			@UploadFile("parts")
			private Part[] parts;
		}
	}

}
