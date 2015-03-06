package me.geso.avans;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;

public class Dispatcher implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory
		.getLogger(Dispatcher.class);
	private final WebRouter<Action> router = new WebRouter<>();

	public Dispatcher() {
	}

	/**
	 * Register classes in the package to dispatcher.
	 */
	public void registerPackage(final Package pkg) {
		this.registerPackage(pkg.getName());
	}

	/**
	 * Register classes in the package to dispatcher.
	 */
	public void registerPackage(final String packageName) {
		Dispatcher.LOGGER.info("Registering package: {}", packageName);
		final ClassLoader contextClassLoader = Thread.currentThread()
			.getContextClassLoader();
		ImmutableSet<ClassInfo> topLevelClasses;
		try {
			topLevelClasses = ClassPath.from(
				contextClassLoader).getTopLevelClasses(packageName);
		} catch (final IOException e) {
			// It caused by programming error.
			throw new RuntimeException(e);
		}
		for (final ClassInfo classInfo : topLevelClasses) {
			final Class<?> klass = classInfo.load();
			if (Controller.class.isAssignableFrom(klass)) {
				final Class<? extends Controller> pagesClass = klass
					.asSubclass(Controller.class);
				this.registerClass(pagesClass);
			} else {
				Dispatcher.LOGGER.info("{} is not a Controller", klass);
			}
		}
	}

	/**
	 * Register class to dispatcher.
	 */
	public void registerClass(final Class<? extends Controller> klass) {
		try {
			Dispatcher.LOGGER.info("Registering class: {}", klass);
			for (final Method method : klass.getMethods()) {
				{
					final POST post = method.getAnnotation(POST.class);
					if (post != null) {
						final String path = post.value();
						final Action action = new Action(klass, method);
						Dispatcher.LOGGER.info("POST {}", path);
						this.router.post(path, action);
					}
				}
				{
					final GET get = method.getAnnotation(GET.class);
					if (get != null) {
						final String path = get.value();
						final Action action = new Action(klass, method);
						Dispatcher.LOGGER.info("GET {}", path);
						this.router.get(path, action);
					}
				}
			}
		} catch (final SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public void handler(final HttpServletRequest request,
			final HttpServletResponse response) {
		final String method = request.getMethod();
		final String path = request.getPathInfo();
		// log.debug("{} {}", method, path);
		final RoutingResult<Action> match = this.router.match(
			method, path);
		if (match == null) {
			this.writeNotFoundErrorPage(response);
			return;
		}

		if (!match.methodAllowed()) {
			this.writeMethodNotAllowedErrorPage(response);
			return;
		}

		final Map<String, String> captured = match.getCaptured();
		final Action action = match.getDestination();
		this.runController(action.getControllerClass(), action.getMethod(),
			request, response, captured);
	}

	// You can replace this method by concrete code in your sub class.
	public void runController(
			final Class<? extends Controller> controllerClass,
			final Method method, final HttpServletRequest request,
			final HttpServletResponse response,
			final Map<String, String> captured) {
		try (Controller controller = buildController(controllerClass)) {
			controller.invoke(method, request, response, captured);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * You can extend Dispatcher class to build controller class by DI container.
	 */
	protected Controller buildController(final Class<? extends Controller> controllerClass)
			throws InstantiationException, IllegalAccessException {
		return controllerClass.newInstance();
	}

	private void writeMethodNotAllowedErrorPage(
			final HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setStatus(405);
		response.setContentType("text/html; charset=utf-8");
		try {
			response.getWriter()
				.write("<!doctype html><html><div style='font-size: 400%'>405 Method Not Allowed</div></html>");
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void writeNotFoundErrorPage(final HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setStatus(404);
		response.setContentType("text/html; charset=utf-8");
		try {
			response.getWriter()
				.write("<!doctype html><html><div style='font-size: 400%'>404 Not Found</div></html>");
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public WebRouter<Action> getRouter() {
		return this.router;
	}

	public static class Action {
		private final Class<? extends Controller> controllerClass;
		private final Method method;

		public Action(final Class<? extends Controller> controllerClass,
				final Method method) {
			this.controllerClass = controllerClass;
			this.method = method;
		}

		public Class<? extends Controller> getControllerClass() {
			return this.controllerClass;
		}

		public Method getMethod() {
			return this.method;
		}
	}

}
