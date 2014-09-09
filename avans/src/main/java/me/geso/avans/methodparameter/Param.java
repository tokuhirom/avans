package me.geso.avans.methodparameter;

import java.lang.annotation.Annotation;

import lombok.Data;

@Data
public class Param {
	private final Annotation[] annotations;
	private final Object object;
	private final String name;

	public Param(String name, Object object, Annotation[] annotations) {
		this.name = name;
		this.object = object;
		this.annotations = annotations;
	}
}