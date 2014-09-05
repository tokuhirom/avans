package me.geso.avans;

import java.lang.reflect.Method;

public interface ActionFactory {
	public Action create(
			Class<? extends Controller> klass,
			Method method
			);
}
