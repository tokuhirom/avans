package me.geso.avans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import me.geso.avans.trigger.BeforeDispatchTrigger;
import me.geso.avans.trigger.HTMLFilter;
import me.geso.avans.trigger.ParamProcessor;
import me.geso.avans.trigger.ResponseConverter;
import me.geso.avans.trigger.ResponseFilter;

class FilterScanner {
	private final List<Method> responseFilters = new ArrayList<>();
	private final List<Method> htmlFilters = new ArrayList<>();
	private final List<Method> beforeDispatchTriggers = new ArrayList<>();
	private final List<Method> responseConverters = new ArrayList<>();
	private final List<Method> paramProcessors = new ArrayList<>();
	private final Set<Method> seen = new HashSet<>();

	void scanMethod(Method method) {
		if (this.seen.contains(method)) {
			return;
		}

		if (method.getAnnotation(BeforeDispatchTrigger.class) != null) {
			this.beforeDispatchTriggers.add(method);
		}
		if (method.getAnnotation(HTMLFilter.class) != null) {
			this.htmlFilters.add(method);
		}
		if (method.getAnnotation(ResponseFilter.class) != null) {
			this.responseFilters.add(method);
		}
		if (method.getAnnotation(ResponseConverter.class) != null) {
			this.responseConverters.add(method);
		}
		if (method.getAnnotation(ParamProcessor.class) != null) {
			this.paramProcessors.add(method);
		}

		this.seen.add(method);
	}

	public void scan(Class<?> klass) {
		// LinkedList じゃなくてもっとうまいやり方あると思う｡
		final LinkedList<Class<?>> linearIsa = new LinkedList<>();
		while (klass != null
			&& klass != ControllerBase.class) {
			linearIsa.addFirst(klass);
			klass = klass.getSuperclass();
		}

		for (final Class<?> k : linearIsa) {
			// scan annotations in interfaces.
			for (final Class<?> interfac : k.getInterfaces()) {
				for (final Method method : interfac.getMethods()) {
					this.scanMethod(method);
				}
			}

			// scan annotations in methods.
			for (final Method method : k.getMethods()) {
				this.scanMethod(method);
			}
		}
	}

	Filters build() {
		return new Filters(
			this.beforeDispatchTriggers,
			this.htmlFilters,
			this.responseFilters,
			this.responseConverters,
			this.paramProcessors);
	}
}
