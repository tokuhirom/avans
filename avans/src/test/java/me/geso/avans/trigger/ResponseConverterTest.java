package me.geso.avans.trigger;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import lombok.Data;
import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.webscrew.response.WebResponse;

import org.junit.Test;

public class ResponseConverterTest {

	@Data
	public static class MyValue {
		private final int foo = 3;
	}

	public static class MyController extends ControllerBase {
		@ResponseConverter(MyValue.class)
		public Optional<WebResponse> responseFilter(MyValue o) {
			return Optional.of(this.renderJSON(o));
		}

		@GET("/")
		public MyValue call() {
			return new MyValue();
		}
	}

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
					assertEquals("{\"foo\":3}", res.getResponseBodyAsString());
				});
	}

}
