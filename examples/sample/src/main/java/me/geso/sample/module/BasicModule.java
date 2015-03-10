package me.geso.sample.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import freemarker.template.Configuration;
import me.geso.sample.config.Config;
import me.geso.sample.provider.ConfigProvider;
import me.geso.sample.provider.web.FreemarkerConfigurationProvider;

public class BasicModule extends AbstractModule {
	private final Config config;

	public BasicModule() {
		this.config = null;
	}

	public BasicModule(final Config config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		if (config != null) {
			bind(Config.class)
				.toInstance(config);
		} else {
			bind(Config.class)
				.toProvider(ConfigProvider.class)
				.asEagerSingleton();
		}
		bind(Configuration.class)
			.toProvider(FreemarkerConfigurationProvider.class)
			.in(Scopes.SINGLETON);
	}
}
