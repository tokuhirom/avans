package me.geso.avans.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import lombok.SneakyThrows;
import me.geso.avans.Action;
import me.geso.avans.ActionFactory;
import me.geso.avans.Controller;

public class BasicActionFactory implements ActionFactory {

	private Constructor<? extends Action> constructor;

	@SneakyThrows
	public BasicActionFactory(Class<? extends Action> actionClass) {
		this.constructor = actionClass.getConstructor(Class.class, Method.class);
	}

	@Override
	@SneakyThrows
	public Action create(Class<? extends Controller> klass, Method method) {
		return this.constructor.newInstance(klass, method);
	}

}
