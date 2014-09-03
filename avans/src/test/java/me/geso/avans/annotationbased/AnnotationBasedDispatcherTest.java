package me.geso.avans.annotationbased;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.avans.AvansDispatcher;
import me.geso.avans.AvansResponse;
import me.geso.avans.AvansWebApplication;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AnnotationBasedDispatcherTest {

	public static class MyServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		public void service(ServletRequest req, ServletResponse res)
				throws ServletException, IOException {
			MyApplication app = new MyApplication(
					(HttpServletRequest) req,
					(HttpServletResponse) res);
			app.run();
		}
	}

	@FunctionalInterface
	public static interface BasicAction {
		public AvansResponse run(AvansWebApplication web);
	}

	public static class MyApplication extends AvansWebApplication {

		static final AnnotationBasedDispatcher dispatcher = new AnnotationBasedDispatcher();
		static {
			try {
				dispatcher.registerPackage("me.geso.avans.annotationbased");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public MyApplication(HttpServletRequest servletRequest,
				HttpServletResponse servletResponse) throws IOException {
			super(servletRequest, servletResponse);
		}

		@Override
		public Path getBaseDirectory() {
			return Paths.get(System.getProperty("user.dir"),
					"src/test/resources/");
		}

		@Override
		public AvansDispatcher createDispatcher() {
			return dispatcher;
		}
	}

	private MechJettyServlet mech;

	@Before
	public void before() {
		this.mech = new MechJettyServlet(MyServlet.class);
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
			assertEquals(res.getContentString(),
					"{\"code\":200,\"messages\":[],\"data\":\"hoge\"}");
		}
	}

	@Test
	public void testPostForm() throws Exception {
		try (MechResponse res = this.mech.post("/postForm")
				.param("name", "John").execute()) {
			assertEquals(res.getContentString(), "(postform)name:John");
		}
	}

}
