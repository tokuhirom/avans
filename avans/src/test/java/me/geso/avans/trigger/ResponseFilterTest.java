package me.geso.avans.trigger;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.webscrew.response.WebResponse;

public class ResponseFilterTest {
	@Test
	public void test() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		JettyServletTester.runServlet(
			servlet,
			(uri) -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2
					.builder()
					.build(), uri);
				final Mech2Result res = mech2.get("/").execute();
				assertEquals(200, res.getResponse().getStatusLine()
					.getStatusCode());
				assertEquals(
					"nosniff",
					res.getResponse()
						.getFirstHeader("X-Content-Type-Options")
						.getValue());
				assertEquals("\"HOGE\"", res.getResponseBodyAsString());
			});
	}

	public static class MyController extends ControllerBase {
		@ResponseFilter
		public void responseFilter(WebResponse resp) {
			resp.addHeader("x-content-type-options", "nosniff");
		}

		@GET("/")
		public WebResponse call() {
			return this.renderJSON("HOGE");
		}
	}

}
