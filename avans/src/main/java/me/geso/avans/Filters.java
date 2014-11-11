package me.geso.avans;

import java.lang.reflect.Method;
import java.util.List;

class Filters {
	private final List<Method> responseFilters;
	private final List<Method> beforeDispatchTriggers;
	private final List<Method> htmlFilters;
	private final List<Method> responseConverters;

	public Filters(
			final List<Method> beforeDispatchTriggers,
			final List<Method> htmlFilters,
			final List<Method> responseFilters,
			final List<Method> responseConverters) {
		this.responseFilters = responseFilters;
		this.beforeDispatchTriggers = beforeDispatchTriggers;
		this.htmlFilters = htmlFilters;
		this.responseConverters = responseConverters;
	}

	public List<Method> getResponseFilters() {
		return this.responseFilters;
	}

	public List<Method> getBeforeDispatchTriggers() {
		return this.beforeDispatchTriggers;
	}

	public List<Method> getHtmlFilters() {
		return this.htmlFilters;
	}

	public List<Method> getResponseConverters() {
		return this.responseConverters;
	}

}