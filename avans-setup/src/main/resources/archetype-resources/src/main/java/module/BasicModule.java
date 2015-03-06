#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import freemarker.template.Configuration;
import ${package}.config.Config;
import ${package}.provider.ConfigProvider;
import ${package}.provider.web.FreemarkerConfigurationProvider;

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
