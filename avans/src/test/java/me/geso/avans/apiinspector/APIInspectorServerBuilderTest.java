package me.geso.avans.apiinspector;

import me.geso.avans.ControllerBase;
import me.geso.avans.Dispatcher;
import me.geso.avans.annotation.GET;
import me.geso.mech.Mech;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class APIInspectorServerBuilderTest {

	public static class Foo extends ControllerBase {
		@GET("/")
		public WebResponse root() {
			return this.renderText("HOGE");
		}
	}

	@Test
	public void test() throws Exception {
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.registerClass(Foo.class);
		Server server = new APIInspectorServerBuilder(dispatcher, 0)
				.build();
		server.start();

		ServerConnector connector = (ServerConnector) server
				.getConnectors()[0];
		int port = connector.getLocalPort();
		String baseURL = "http://127.0.0.1:" + port;
		Mech mech = new Mech(baseURL);
		try (MechResponse res = mech.get("/").execute()) {
			assertEquals(res.getStatusCode(), 200);
			assertTrue(res.getContentString().contains("WebResponse"));
		}

		server.stop();
	}
}

