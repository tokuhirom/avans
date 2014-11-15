package me.geso.avans.annotationbased;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.Dispatcher;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AnnotationBasedDispatcherTest {

	public static class MyServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		static final Dispatcher dispatcher = new Dispatcher();
		static {
			MyServlet.dispatcher
					.registerPackage("me.geso.avans.annotationbased");
			System.out.println(MyServlet.dispatcher.getRouter().toString());
		}

		@Override
		public void service(final ServletRequest req, final ServletResponse res)
				throws ServletException, IOException {
			MyServlet.dispatcher.handler(
					(HttpServletRequest) req,
					(HttpServletResponse) res);
		}
	}

	@FunctionalInterface
	public static interface BasicAction {
		public WebResponse run(final ControllerBase web);
	}

	public static class MyApplication extends ControllerBase {
	}

	private MechJettyServlet mech;

	@Before
	public void before() {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerPackage(MyController.class.getPackage());
		this.mech = new MechJettyServlet(servlet);
	}

	@After
	public void after() throws Exception {
		if (this.mech != null) {
			this.mech.close();
		}
	}

	@Test
	public void test() throws Exception {
		try (MechResponse res = this.mech.get("/").execute()) {
			Assert.assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"hoge\"}");
		}
	}

	@Test
	public void testPostForm() throws Exception {
		try (MechResponse res = this.mech.post("/postForm")
				.param("name", "John").execute()) {
			Assert.assertEquals(res.getContentString(), "(postform)name:John");
		}
	}

}
