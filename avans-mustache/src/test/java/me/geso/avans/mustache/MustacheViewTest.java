package me.geso.avans.mustache;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;

import org.junit.Test;

public class MustacheViewTest {

	public static class Foo {
		private String name;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class MyController extends ControllerBase implements
			MustacheView {
		@GET("/mustache")
		public WebResponse mustache() {
			final Foo foo = new Foo();
			foo.setName("John");
			return this.renderMustache("mustache.mustache", foo);
		}

		@Override
		public Path getMustacheTemplateDirectory() {
			return Paths.get("src/test/resources/templates/");
		}
	}

	@Test
	public void test() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);

		try (final MechJettyServlet mech = new MechJettyServlet(servlet)) {
			try (MechResponse res = mech.get("/mustache").execute()) {
				assertThat(res.getStatusCode(), is(200));
				assertThat(res.getContentType().getMimeType(), is("text/html"));
				assertThat(res.getContentType().getCharset().displayName(),
						is("UTF-8"));
				assertThat(res.getContentString(), is("Hi, John!\n"));
			}
		}
	}

}
