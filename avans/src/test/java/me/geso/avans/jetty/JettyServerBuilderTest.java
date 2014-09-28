package me.geso.avans.jetty;

import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech.Mech;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.Assert;
import org.junit.Test;

public class JettyServerBuilderTest {

	public static class Foo extends ControllerBase {
		@GET("/")
		public WebResponse root() {
			return this.renderText("HOGE");
		}
	}

	@Test
	public void test() throws Exception {
		final Server server = new JettyServerBuilder()
				.setPort(0)
				.registerClass(Foo.class)
				.build();
		server.start();

		final ServerConnector connector = (ServerConnector) server
				.getConnectors()[0];
		final int port = connector.getLocalPort();
		final String baseURL = "http://127.0.0.1:" + port;
		try (final Mech mech = new Mech(baseURL)) {
			try (MechResponse res = mech.get("/").execute()) {
				Assert.assertEquals(res.getStatusCode(), 200);
				Assert.assertEquals(res.getContentString(), "HOGE");
			}
		}

		server.stop();
	}
}
