package me.geso.sample.module;

import javax.servlet.ServletContext;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import freemarker.template.Configuration;
import me.geso.sample.provider.web.FreemarkerConfigurationProvider;

import lombok.NonNull;

public class WebModule extends AbstractModule {
	private final ServletContext servletContext;

	public WebModule(@NonNull final ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	protected void configure() {
		bind(ServletContext.class)
				.toInstance(servletContext);
		bind(Configuration.class)
				.toProvider(FreemarkerConfigurationProvider.class)
				.in(Scopes.SINGLETON);
	}
}
