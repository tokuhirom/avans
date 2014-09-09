package me.geso.avans.methodparameter;

import java.lang.reflect.Method;

import me.geso.avans.Controller;

public interface MethodParameterBuilder {
	public Param[] build(Controller controller, Method method);
}