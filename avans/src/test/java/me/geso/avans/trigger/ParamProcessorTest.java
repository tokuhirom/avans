package me.geso.avans.trigger;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Parameter;
import java.util.Optional;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.ParameterProcessorResult;
import me.geso.avans.annotation.GET;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.webscrew.response.WebResponse;

import org.junit.Test;

public class ParamProcessorTest {

	public static class MyController extends ControllerBase {
		@ParamProcessor(targetClass = String.class)
		public ParameterProcessorResult paramUpperQ(Parameter parameter) {
			final Optional<String> q = this.getRequest().getQueryParams()
					.getFirst("q");
			if (q.isPresent()) {
				return ParameterProcessorResult.fromData(q.get().toUpperCase());
			} else {
				final WebResponse response = this.renderError(400, "Missing Q");
				return ParameterProcessorResult.fromWebResponse(response);
			}
		}

		@GET("/")
		public WebResponse index(String q) {
			return this.renderText(q);
		}
	}

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
							final Mech2Result res = mech2.get("/").execute();
							assertEquals(400, res.getResponse().getStatusLine()
									.getStatusCode());
							assertEquals(
									"{\"code\":400,\"messages\":[\"Missing Q\"]}",
									res.getResponseBodyAsString());
						});
	}

}
