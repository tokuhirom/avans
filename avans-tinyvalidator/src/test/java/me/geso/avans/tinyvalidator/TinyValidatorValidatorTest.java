package me.geso.avans.tinyvalidator;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.BeanParam;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.tinyvalidator.ConstraintViolation;
import me.geso.tinyvalidator.Validator;
import me.geso.tinyvalidator.constraints.NotNull;
import me.geso.tinyvalidator.constraints.Pattern;
import me.geso.webscrew.response.WebResponse;

@Slf4j
public class TinyValidatorValidatorTest {

	public static class MyController extends ControllerBase implements
			TinyValidatorValidator {
		@POST("/jsonParam")
		public WebResponse jsonParam(@NonNull @JsonParam Foo foo) {
			foo.setName(foo.getName().toUpperCase());
			return this.renderJSON(foo);
		}
	}

	@Data
	public static class Foo {
		@NotNull
		private String name;
	}

	@FunctionalInterface
	public interface SubTestBody {
		@SuppressWarnings("RedundantThrows")
		void run() throws Exception;
	}

	void subtest(String title, SubTestBody body) throws Exception {
		System.out.println("---- " + title + " ----");
		body.run();
	}

	@Test
	public void testJsonParamValidationFailed() throws Exception {
		// validation failed.
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);

		JettyServletTester
			.runServlet(
				servlet,
				(uri) -> {
					this.subtest(
						"PASS validation rules",
						() -> {
							final Mech2 m = Mech2.builder().build();
							final Mech2WithBase mech2 = new Mech2WithBase(
								m, uri);
							final Foo foo = new Foo();
							foo.setName("John");
							final Mech2Result res = mech2.postJSON(
								"/jsonParam", foo)
								.execute();
							assertEquals(200, res.getResponse()
								.getStatusLine()
								.getStatusCode());
							assertEquals(
								"application/json; charset=utf-8",
								res
									.getResponse()
									.getFirstHeader(
										"Content-Type")
									.getValue());
							assertEquals("{\"name\":\"JOHN\"}",
								res.getResponseBodyAsString());
						});
					this.subtest(
						"FAIL",
						() -> {
							final Mech2 m = Mech2.builder().build();
							final Mech2WithBase mech2 = new Mech2WithBase(
								m, uri);
							final Foo foo = new Foo();
							foo.setName(null);
							final Mech2Result res = mech2.postJSON(
								"/jsonParam", foo)
								.execute();
							assertEquals(200, res.getResponse()
								.getStatusLine()
								.getStatusCode());
							assertEquals(
								"application/json; charset=utf-8",
								res
									.getResponse()
									.getFirstHeader("Content-Type")
									.getValue());
							assertEquals(
								"{\"code\":403,\"messages\":[\"name may not be null.\"]}",
								res.getResponseBodyAsString());
						});
				});
	}

	// -------------------------------------------------
	// inheritance
	// -------------------------------------------------

	@Data
	public static class Parent {
		@Param("parentName")
		@NotNull
		private String parentName;
	}

	@EqualsAndHashCode(callSuper = false)
	@Data
	public static class Child extends Parent {
		@Param("childName")
		@NotNull
		private String childName;
	}

	public static class MyController2 extends ControllerBase implements TinyValidatorValidator {
		@GET("/")
		public WebResponse jsonParam(@NonNull @BeanParam Child child) {
			return this.renderJSON(child);
		}
	}

	@Test
	public void testInheritanceWithBeanParam() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController2.class);

		JettyServletTester
			.runServlet(
				servlet,
				(uri) -> {
					final Mech2 m = Mech2.builder().build();
					final Mech2WithBase mech2 = new Mech2WithBase(
						m, uri);
					final Mech2Result res = mech2.get(
							"/")
						.addQueryParameter("childName", "John")
							.addQueryParameter("parentName", "Nick")
						.execute();
					Assert.assertThat(res.getStatusCode(), is(200));
					final Child child = res.parseJSON(Child.class);
					Assert.assertThat(child.getParentName(), is("Nick"));
					Assert.assertThat(child.getChildName(), is("John"));
				}
			);
	}

	// -------------------------------------------------
	// pattern
	// -------------------------------------------------

	@Data
	public static class PatternForm {
		@Param("childName")
		@Pattern(regexp=".")
		@NotNull
		private String childName;
	}

	public static class PatternController extends ControllerBase implements TinyValidatorValidator {
		@GET("/")
		public WebResponse jsonParam(@NonNull @BeanParam PatternForm child) {
			return this.renderJSON(child);
		}
	}

	@Test
	public void testPattern() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(PatternController.class);

		final PatternForm form = new PatternForm();
		final List<ConstraintViolation> constraintViolations = new Validator().validate(form);
		log.info("{}", constraintViolations);

		JettyServletTester
				.runServlet(
						servlet,
						(uri) -> {
							final Mech2 m = Mech2.builder().build();
							final Mech2WithBase mech2 = new Mech2WithBase(
									m, uri);
							final Mech2Result res = mech2.get(
									"/")
									.addQueryParameter("childName", "")
									.execute();
							Assert.assertThat(res.getStatusCode(), is(200));
							log.info("response: {}", res.getResponseBodyAsString());
							assertThat(res.getResponseBodyAsString(), containsString("\"childName must match .\""));
						}
				);
	}

}
