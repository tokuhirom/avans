#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.provider.web;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
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
	private final ServletContext servletContext;

	@Inject
	public FreemarkerConfigurationProvider(final Config config, final ServletContext servletContext) {
		this.config = config;
		this.servletContext = servletContext;
	}

	public Configuration get() {
		Configuration configuration = new Configuration();

		try {
			// Do not commify numbers!
			configuration.setNumberFormat("0.${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}${symbol_pound}");
			configuration.setDefaultEncoding("UTF-8");
			configuration.setTemplateLoader(this.buildTemplateLoader());

			if (config.isDevelopment()) {
				configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			} else {
				configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			}
			configuration.setTemplateLoader(new HtmlTemplateLoader(configuration.getTemplateLoader()));
			configuration.setIncompatibleImprovements(new Version(2, 3, 20)); // FreeMarker
			configuration.setSharedVariable("isDevelopment", config.isDevelopment());
			return configuration;
		} catch (TemplateModelException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private TemplateLoader buildTemplateLoader() throws IOException {
		// Set template file path
		if (config.isDevelopment()) {
			final String realPath = servletContext.getRealPath("WEB-INF/web.xml");
			if (realPath != null) {
				final String s = realPath.replaceFirst("/target/.*", "/");
				final File file = new File(s, "src/main/webapp/WEB-INF/templates/");
				if (file.isDirectory()) {
					// Use src/main/resources/templates on development environment.
					log.info("Load templates from {}", file.getAbsolutePath());
					return new FileTemplateLoader(file);
				}
			}
		}

		return new WebappTemplateLoader(servletContext, "WEB-INF/templates/");
	}
}
