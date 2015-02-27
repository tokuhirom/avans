package me.geso.avans.trigger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.ParameterProcessorResult;
import me.geso.avans.annotation.GET;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.webscrew.response.WebResponse;

public class ParamProcessorTest {

	@Test
	public void test() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		JettyServletTester
			.runServlet(
				servlet,
				(uri) -> {
					{
						final Mech2WithBase mech2 = new Mech2WithBase(Mech2
							.builder()
							.build(), uri);
						final Mech2Result res = mech2.get("/").execute();
						assertEquals(400, res.getResponse().getStatusLine()
							.getStatusCode());
						assertEquals(
							"{\"code\":400,\"messages\":[\"Missing Q\"]}",
							res.getResponseBodyAsString());
					}
					{
						final Mech2WithBase mech2 = new Mech2WithBase(Mech2
							.builder()
							.build(), uri);
						final Mech2Result res = mech2.get("/")
							.addQueryParameter("q", "hoge").execute();
						assertEquals(200, res.getResponse().getStatusLine()
							.getStatusCode());
						assertEquals(
							"HOGE",
							res.getResponseBodyAsString());
					}
				});
	}

	@Test
	public void testAnnotation() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		JettyServletTester
			.runServlet(
				servlet,
				(uri) -> {
					final Mech2WithBase mech2 = new Mech2WithBase(Mech2
						.builder()
						.build(), uri);
					final Mech2Result res = mech2.get("/annotation").execute();
					assertEquals(200, res.getResponse().getStatusLine()
						.getStatusCode());
					assertEquals(
						"3.14",
						res.getResponseBodyAsString());
				});
	}

	@Test
	public void testAnnotation2() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		JettyServletTester
			.runServlet(
				servlet,
				(uri) -> {
					final Mech2WithBase mech2 = new Mech2WithBase(Mech2
						.builder()
						.build(), uri);
					final Mech2Result res = mech2.get("/annotation2").execute();
					assertEquals(200, res.getResponse().getStatusLine()
						.getStatusCode());
					assertEquals(
						"2.71828",
						res.getResponseBodyAsString());
				});
	}

	@Test
	public void testIsAssignableFrom() throws Exception {
		assertFalse(String.class.isAssignableFrom(Object.class));
		assertTrue(Object.class.isAssignableFrom(String.class));
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	public @interface MyAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	public @interface MyAnnotation2 {
	}

	@Slf4j
	public static class MyController extends ControllerBase {

		@SuppressWarnings("UnusedParameters")
		@ParamProcessor(targetClass = String.class)
		public ParameterProcessorResult paramUpperQ(Parameter parameter) {
			log.info("paramUpperQ");
			final Optional<String> q = Optional.ofNullable(this
				.getServletRequest().getParameter("q"));
			if (q.isPresent()) {
				return ParameterProcessorResult.fromData(q.get().toUpperCase());
			} else {
				final WebResponse response = this.renderError(400, "Missing Q");
				return ParameterProcessorResult.fromWebResponse(response);
			}
		}

		@SuppressWarnings("UnusedParameters")
		@ParamProcessor(targetAnnotation = MyAnnotation.class)
		public ParameterProcessorResult paramAnnotation(Parameter parameter) {
			log.info("paramAnnotation");
			return ParameterProcessorResult.fromData(3.14);
		}

		@SuppressWarnings("UnusedParameters")
		@ParamProcessor(targetAnnotation = MyAnnotation2.class)
		public ParameterProcessorResult paramAnnotation2(Parameter parameter) {
			log.info("paramAnnotation2");
			return ParameterProcessorResult.fromData(2.71828);
		}

		@GET("/")
		public WebResponse index(String q) {
			return this.renderText(q);
		}

		@GET("/annotation")
		public WebResponse annotation(@MyAnnotation Double pi) {
			return this.renderText("" + pi);
		}

		@GET("/annotation2")
		public WebResponse annotation2(@MyAnnotation2 Double e) {
			return this.renderText("" + e);
		}
	}

}
