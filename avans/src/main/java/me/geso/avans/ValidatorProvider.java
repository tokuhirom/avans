package me.geso.avans;

import java.lang.reflect.Method;
import java.util.Optional;

import me.geso.webscrew.response.WebResponse;

public interface ValidatorProvider {
	/**
	 * Return WebResponse if there is violations.
	 * 
	 * @param method
	 * @param params
	 * @return
	 */
	public default Optional<WebResponse> validateParameters(Method method,
			Object[] params) {
		return Optional.empty();
	}
}
