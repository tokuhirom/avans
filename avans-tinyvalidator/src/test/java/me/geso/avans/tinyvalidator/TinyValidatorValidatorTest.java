package me.geso.avans.tinyvalidator;

import static org.junit.Assert.*;

import org.junit.Test;

import lombok.Data;
import lombok.NonNull;
import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.POST;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.tinyvalidator.constraints.NotNull;
import me.geso.webscrew.response.WebResponse;

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

}
