package me.geso.avans;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Controller extends AutoCloseable {

	void init(final HttpServletRequest request,
			final HttpServletResponse response,
			final Map<String, String> captured);

	public HttpServletRequest getServletRequest();

	public Map<String, String> getPathParams();

	public void invoke(final Method method, final HttpServletRequest request,
			final HttpServletResponse response,
			final Map<String, String> captured);

	/**
	 * Set value for stash space for the plugins. You can store the plugin
	 * specific data into here.
	 * 
	 */
	public void setPluginStashValue(Class<?> pluginClass, String key,
			Object value);

	/**
	 * Get plugin stash value.
	 */
	public Optional<Object> getPluginStashValue(Class<?> pluginClass, String key);

	/**
	 * Get plugin stash value. Compute if absent with callback.
	 */
	public Object computePluginStashValueIfAbsent(Class<?> pluginClass,
			String key,
			Supplier<?> supplier);
}
