package me.geso.avans.freemarker;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class FreeMarkerViewTest {

	public static abstract class MyControllerBase extends ControllerBase {
		// Configuration is thread safe.
		private static final Configuration configuration = buildConfiguration();

		public <T> WebResponse render(String templateName, Object dataModel)
				throws IOException, TemplateException {
			return new FreeMarkerView(configuration).render(this, templateName,
					dataModel);
		}

		private static Configuration buildConfiguration() {
			final Configuration cfg = new Configuration();
			try {
				cfg.setDirectoryForTemplateLoading(new File(
						"src/test/resources/"));
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			cfg.setDefaultEncoding("UTF-8");

			// cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setIncompatibleImprovements(new Version(2, 3, 20));
			return cfg;
		}
	}

	public static class MyController extends MyControllerBase {

		@GET("/")
		public Object root() throws IOException, TemplateException {
			final Map<String, String> map = new HashMap<>();
			map.put("name", "John");
			return this.render("hello.fttl", map);
		}
	}

	@Test
	public void test() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		try (MechJettyServlet mech = new MechJettyServlet(servlet)) {
			try (MechResponse res = mech.get("/").execute()) {
				assertThat(res.getContentString(), is("Hello, John!"));
			}
		}
	}

}
