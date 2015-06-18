package me.geso.sample.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import freemarker.template.Configuration;
import me.geso.sample.provider.web.FreemarkerConfigurationProvider;

public class WebModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Configuration.class)
			.toProvider(FreemarkerConfigurationProvider.class)
			.in(Scopes.SINGLETON);
	}
}
