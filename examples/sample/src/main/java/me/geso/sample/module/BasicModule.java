package me.geso.sample.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import freemarker.template.Configuration;
import me.geso.sample.config.Config;
import me.geso.sample.provider.ConfigProvider;
import me.geso.sample.provider.web.FreemarkerConfigurationProvider;

public class BasicModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Config.class)
			.toProvider(ConfigProvider.class)
			.asEagerSingleton();
		bind(Configuration.class)
			.toProvider(FreemarkerConfigurationProvider.class)
			.in(Scopes.SINGLETON);
	}
}
