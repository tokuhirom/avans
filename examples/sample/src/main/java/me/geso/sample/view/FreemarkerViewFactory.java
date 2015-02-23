package me.geso.sample.view;

import freemarker.template.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.geso.sample.ConfigLoader;
import me.geso.sample.controller.BaseController;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class FreemarkerViewFactory {
	private final Configuration configuration;

	public FreemarkerViewFactory() {
		this.configuration = this.buildFreemarkerConfiguration();
	}

	public FreemarkerView create(@NonNull String templatePath, @NonNull BaseController controller) {
		return new FreemarkerView(this.configuration, templatePath, controller);
	}

	public Configuration buildFreemarkerConfiguration() {
		try {
			final Configuration cfg = new Configuration();

			// Do not commify numbers!
			cfg.setNumberFormat("0.######");

			// Set template file path
			if (ConfigLoader.getConfig().isDevelopment() && new File("src/main/resources/templates/").exists()) {
				// Use src/main/resources/templates on development environment.
				log.info("Load templates from src/main/resources/templates/");
				cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates/"));
			} else {
				// Use resource files on production environment.
				URL resource = BaseController.class.getClassLoader().getResource("templates/");
				assert resource != null;
				cfg.setDirectoryForTemplateLoading(new File(resource.getFile()));
			}
			cfg.setDefaultEncoding("UTF-8");

			if (ConfigLoader.getConfig().isDevelopment()) {
				cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			} else {
				cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			}
			cfg.setTemplateLoader(new HtmlTemplateLoader(cfg.getTemplateLoader()));
			cfg.setIncompatibleImprovements(new Version(2, 3, 20)); // FreeMarker
			cfg.setSharedVariable("isDevelopment", BaseController.isDevelopment());

			return cfg;
		} catch (TemplateModelException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
