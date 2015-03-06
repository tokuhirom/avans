#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Injector;

import me.geso.avans.Controller;
import ${package}.module.WebModule;

public class Dispatcher extends me.geso.avans.Dispatcher {
	private final Injector injector;

	public Dispatcher(final Injector injector) {
		this.injector=injector;
	}

	@Override
	public void runController(
			final Class<? extends Controller> controllerClass,
			final Method method, final HttpServletRequest request,
			final HttpServletResponse response,
			final Map<String, String> captured) {
		final Injector childInjector = injector.createChildInjector(new WebModule(request));
		try (Controller controller = childInjector.getInstance(controllerClass)) {
			controller.invoke(method, request, response, captured);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
