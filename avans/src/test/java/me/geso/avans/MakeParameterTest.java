package me.geso.avans;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import me.geso.avans.annotation.GET;
import me.geso.avans.methodparameter.DefaultMethodParameterBuilder;
import me.geso.avans.methodparameter.MethodParameterBuilder;
import me.geso.avans.methodparameter.Param;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;

import org.junit.Test;

public class MakeParameterTest {
	public static class Iyan {
		String v;
	}

	public static class MyMethodParameterBuilder extends DefaultMethodParameterBuilder {

		@Override
		protected Optional<Param> MAKE_PARAMETER(Controller controller, Method method, Parameter parameter) {
			if (parameter.getType() == Iyan.class) {
				Iyan iyan = new Iyan();
				iyan.v = "mattn";
				return Optional.of(new Param("iyan", iyan, parameter.getAnnotations()));
			}
			return Optional.empty();
		}

	}

	public static class MyController extends ControllerBase {
		@GET("/")
		public WebResponse root(Iyan iyan) {
			return this.renderText(iyan.v);
		}
		
		public MethodParameterBuilder createMethodParameterBuilder() {
			return new MyMethodParameterBuilder();
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
