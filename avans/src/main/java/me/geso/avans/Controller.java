package me.geso.avans;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.webscrew.Parameters;
import me.geso.webscrew.request.WebRequest;
import me.geso.webscrew.response.WebResponse;

public interface Controller extends AutoCloseable {

	void init(final HttpServletRequest request,
			final HttpServletResponse response,
			final Map<String, String> captured);

	public WebRequest getRequest();

	public Parameters getPathParameters();

	public WebResponse renderJSON(final Object obj);

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
	public Object computePluginStashIfAbsent(Class<?> pluginClass, String key,
			Supplier<?> supplier);

	public Path getBaseDirectory();
}
