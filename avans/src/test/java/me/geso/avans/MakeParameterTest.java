package me.geso.avans;

import java.lang.reflect.Parameter;
import java.util.Optional;

import me.geso.avans.annotation.GET;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;

import org.junit.Assert;
import org.junit.Test;

public class MakeParameterTest {
	public static class Iyan {
		String v;
	}

	public static class MyController extends ControllerBase {
		@GET("/")
		public WebResponse root(final Iyan iyan) {
			return this.renderText(iyan.v);
		}

		@Override
		protected Optional<Object> GET_PARAMETER(final Parameter parameter) {
			if (parameter.getType() == Iyan.class) {
				final Iyan iyan = new Iyan();
				iyan.v = "mattn";
				return Optional.of(iyan);
			}
			return Optional.empty();
		}
	}

	@Test
	public void test() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		try (MechJettyServlet mech = new MechJettyServlet(servlet)) {
			try (MechResponse res = mech.get("/").execute()) {
				Assert.assertEquals(200, res.getStatusCode());
				Assert.assertEquals("mattn", res.getContentString());
			}
		}
	}
}
