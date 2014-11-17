package me.geso.avans;

import java.lang.reflect.Method;
import java.util.Optional;

import me.geso.webscrew.response.WebResponse;

public interface ValidatorProvider {
	public Optional<WebResponse> validateParameters(Method method,
			Object[] params);
}
