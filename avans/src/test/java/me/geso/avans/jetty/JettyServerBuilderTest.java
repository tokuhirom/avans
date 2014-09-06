package me.geso.avans.jetty;

import static org.junit.Assert.assertEquals;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech.Mech;
import me.geso.mech.MechResponse;
import me.geso.webscrew.WebResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
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
		Server server = new JettyServerBuilder()
				.setPort(0)
				.registerClass(Foo.class)
				.build();
		server.start();

		ServerConnector connector = (ServerConnector) server
				.getConnectors()[0];
		int port = connector.getLocalPort();
		String baseURL = "http://127.0.0.1:" + port;
		Mech mech = new Mech(baseURL);
		try (MechResponse res = mech.get("/").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertEquals(res.getContentString(), "HOGE");
		}

		server.stop();
	}
}
