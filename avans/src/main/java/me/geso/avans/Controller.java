package me.geso.avans;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Controller {

	void init(HttpServletRequest request, HttpServletResponse response,
			Map<String, String> captured);

	void dispatch(Method method);

}
