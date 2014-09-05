package me.geso.avans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import lombok.SneakyThrows;

public class BasicActionFactory implements ActionFactory {

	private Constructor<? extends Action> constructor;

	@SneakyThrows
	public BasicActionFactory(Class<? extends Action> actionClass) {
		Constructor<? extends Action> constructor = actionClass.getConstructor(Class.class, Method.class);
		this.constructor = constructor;
	}

	@Override
	@SneakyThrows
	public Action create(Class<? extends Controller> klass, Method method) {
		return this.constructor.newInstance(klass, method);
	}

}
