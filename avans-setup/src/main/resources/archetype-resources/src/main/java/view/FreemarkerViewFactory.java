#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.view;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ${package}.ConfigLoader;
import ${package}.controller.BaseController;

@Slf4j
public class FreemarkerViewFactory {
	private final Configuration configuration;

	public FreemarkerViewFactory() {
		this.configuration = new Configuration();

		try {
			// Do not commify numbers!
			configuration.setNumberFormat("0.${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}");
			configuration.setDefaultEncoding("UTF-8");
			this.setTemplatePath();

			if (ConfigLoader.getConfig().isDevelopment()) {
				configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			} else {
				configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			}
			configuration.setTemplateLoader(new HtmlTemplateLoader(configuration.getTemplateLoader()));
			configuration.setIncompatibleImprovements(new Version(2, 3, 20)); // FreeMarker
			configuration.setSharedVariable("isDevelopment", BaseController.isDevelopment());
		} catch (TemplateModelException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public FreemarkerView create(@NonNull String templatePath, @NonNull BaseController controller) {
		return new FreemarkerView(this.configuration, templatePath, controller);
	}

	private void setTemplatePath() throws IOException {
		// Set template file path
		if (ConfigLoader.getConfig().isDevelopment()) {
			String tmplPath = "src/main/resources/templates/";
			if (new File(tmplPath).exists()) {
				// Use src/main/resources/templates on development environment.
				log.info("Load templates from {}", tmplPath);
				configuration.setDirectoryForTemplateLoading(new File(tmplPath));
				return;
			} else {
				log.info("There is no {}", tmplPath);
			}
		}

		log.info("Load templates from class loader");
		// Use resource files on production environment.
		URL resource = BaseController.class.getClassLoader().getResource("templates/");
		if (resource == null) {
			throw new RuntimeException("There is no templates/ directory in resources.");
		}
		configuration.setDirectoryForTemplateLoading(new File(resource.getFile()));
	}
}
