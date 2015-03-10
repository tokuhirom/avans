package me.geso.sample.module;

import javax.servlet.ServletContext;

import com.google.inject.AbstractModule;

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
	}
}
