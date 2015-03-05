#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.provider.web;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Provider;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import lombok.extern.slf4j.Slf4j;
import ${package}.config.Config;
import ${package}.view.HtmlTemplateLoader;

@Slf4j
public class FreemarkerConfigurationProvider implements Provider<Configuration> {
	private final Config config;

	@Inject
	public FreemarkerConfigurationProvider(Config config) {
		this.config = config;
	}

	public Configuration get() {
		Configuration configuration = new Configuration();

		try {
			// Do not commify numbers!
			configuration.setNumberFormat("0.${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}");
			configuration.setDefaultEncoding("UTF-8");
			this.setTemplatePath(configuration);

			if (config.isDevelopment()) {
				configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			} else {
				configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			}
			configuration.setTemplateLoader(new HtmlTemplateLoader(configuration.getTemplateLoader()));
			configuration.setIncompatibleImprovements(new Version(2, 3, 20)); // FreeMarker
			configuration.setSharedVariable("isDevelopment", config.isDevelopment());
			return configuration;
		} catch (TemplateModelException | IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private void setTemplatePath(Configuration configuration) throws IOException, URISyntaxException {
		URL resource = getClass().getClassLoader().getResource("templates/");

		// Set template file path
		if (config.isDevelopment()) {
			Path path = Paths.get(resource.toURI());
			while (path.getNameCount() > 1) {
				if (path.getFileName().toString().equals("target")) {
					final Path baseDirectory = path.getParent();
					File file = baseDirectory.resolve("src/main/resources/templates/").toFile();
					if (file.exists()) {
						// Use src/main/resources/templates on development environment.
						log.info("Load templates from {}", file.getAbsolutePath());
						configuration.setDirectoryForTemplateLoading(file);
						return;
					}
				}
				path = path.getParent();
			}
		}

		log.info("Load templates from class loader: {}", resource);
		// Use resource files on production environment.
		if (resource == null) {
			throw new RuntimeException("There is no templates/ directory in resources.");
		}
		configuration.setDirectoryForTemplateLoading(new File(resource.getFile()));
	}
}
