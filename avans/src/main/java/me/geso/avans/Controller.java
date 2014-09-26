package me.geso.avans;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.webscrew.Parameters;
import me.geso.webscrew.request.WebRequest;
import me.geso.webscrew.response.WebResponse;

public interface Controller extends AutoCloseable {

	void init(HttpServletRequest request, HttpServletResponse response,
			Map<String, String> captured);

	public WebRequest getRequest();

	Parameters getPathParameters();

	public WebResponse renderJSON(Object obj);

	public void invoke(Method method, HttpServletRequest request,
			HttpServletResponse response, Map<String, String> captured);

	/**
	 * Stash space for the plugins. You can store the plugin specific data into
	 * here.
	 * 
	 * @return
	 */
	public Map<String, Object> getPluginStash();
}
