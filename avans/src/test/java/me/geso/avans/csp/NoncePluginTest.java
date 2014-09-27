package me.geso.avans.csp;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class NoncePluginTest {

	public static class MyController extends ControllerBase implements
	NoncePlugin {
		@GET("/")
		public WebResponse root() {
			return this.renderText("NONCE:" + this.getNonce());
		}
	}

	@Test
	public void test() throws Exception {
		final AvansServlet avansServlet = new AvansServlet();
		avansServlet.registerClass(MyController.class);
		try (MechJettyServlet mech = new MechJettyServlet(avansServlet)) {
			try (
					MechResponse res = mech.get("/").execute()) {
				Assert.assertThat(res.getStatusCode(), CoreMatchers.is(200));
				System.out.println(res
						.getFirstHeader("Content-Security-Policy"));
				Assert.assertTrue(res.getFirstHeader("Content-Security-Policy")
						.get()
						.matches(
								"\\Aunsafe-inline; script-src 'nonce-.+'\\z"));
				Assert.assertTrue(res.getContentString().matches(
						"\\ANONCE:.+\\z"));
			}
		}
	}

}
