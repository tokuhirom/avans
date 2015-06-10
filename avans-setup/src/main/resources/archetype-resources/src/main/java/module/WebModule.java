#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import freemarker.template.Configuration;
import ${package}.provider.web.FreemarkerConfigurationProvider;

public class WebModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Configuration.class)
			.toProvider(FreemarkerConfigurationProvider.class)
			.in(Scopes.SINGLETON);
	}
}
