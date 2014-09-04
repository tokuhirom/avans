package me.geso.avans;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import me.geso.avans.annotation.GET;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;

import org.junit.Test;

public class MakeParameterTest {
	public static class Iyan {
		String v;
	}

	public static class MyController extends ControllerBase {
		@Override
		protected Object MAKE_PARAMETER(Method method, Parameter parameter) {
			if (parameter.getType() == Iyan.class) {
				Iyan iyan = new Iyan();
				iyan.v = "mattn";
				return iyan;
			}
			return null;
		}

		@GET("/")
		public WebResponse root(Iyan iyan) {
			return this.renderTEXT(iyan.v);
		}
	}

	@Test
	public void test() throws Exception {
		AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		try (MechJettyServlet mech = new MechJettyServlet(servlet)) {
			try (MechResponse res = mech.get("/").execute()) {
				assertEquals(200, res.getStatusCode());
				assertEquals("mattn", res.getContentString());
			}
		}
	}
}
