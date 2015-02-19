package me.geso.avans.trigger;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.WebResponse;

import org.junit.Test;

public class BeforeDispatchTriggerTest {

	public static class MyController extends ControllerBase {
		private String resp = "NG";

		@BeforeDispatchTrigger
		public Optional<WebResponse> beforeDispatch() {
			this.resp = "OK";
			return Optional.empty();
		}

		@GET("/")
		public WebResponse index() {
			return new ByteArrayResponse(200,
				this.resp.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Test
	public void testBeforeDispatchDoesNotReturnResponse() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		JettyServletTester.runServlet(servlet, (uri) -> {
			final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
				.build(), uri);
			final Mech2Result execute = mech2.get("/").execute();
			assertEquals(200, execute.getResponse().getStatusLine()
				.getStatusCode());
			assertEquals("OK", execute.getResponseBodyAsString());
		});
	}

	// ----------------------------------------------------------------

	public static class MyController2 extends ControllerBase {
		@BeforeDispatchTrigger
		public Optional<WebResponse> beforeDispatch() {
			return Optional.of(new ByteArrayResponse(200,
				"FromTrigger".getBytes(StandardCharsets.UTF_8)));
		}

		@GET("/")
		public WebResponse index() {
			return new ByteArrayResponse(200,
				"FAIL".getBytes(StandardCharsets.UTF_8));
		}
	}

	@Test
	public void testBeforeDispatchReturnsResponse() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController2.class);
		JettyServletTester.runServlet(servlet, (uri) -> {
			final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
				.build(), uri);
			final Mech2Result execute = mech2.get("/").execute();
			assertEquals(200, execute.getResponse().getStatusLine()
				.getStatusCode());
			assertEquals("FromTrigger", execute.getResponseBodyAsString());
		});
	}

}
