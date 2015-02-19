package me.geso.avans.trigger;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.webscrew.response.WebResponse;

import org.apache.http.ParseException;
import org.junit.Test;

public class HTMLFilterTest {

	public static class MyController extends ControllerBase {
		@HTMLFilter
		public String htmlFilter(String src) {
			return src.toUpperCase();
		}

		@GET("/")
		public WebResponse foo() {
			return this.renderText(this.filterHTML("Hige"));
		}
	}

	@Test
	public void test() throws ParseException, URISyntaxException, IOException,
			Exception {
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
				assertEquals("HIGE", res.getResponseBodyAsString());
			});
	}

}
