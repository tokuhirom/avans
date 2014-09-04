package me.geso.avans;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class Dispatcher {
	private static final Logger logger = LoggerFactory
			.getLogger(Dispatcher.class);
	private final WebRouter<Destination> router = new WebRouter<>();

	public Dispatcher() {
	}

	public void registerPackage(String packageName) throws IOException {
		logger.info("Registering package: {}", packageName);
		ClassLoader contextClassLoader = Thread.currentThread()
				.getContextClassLoader();
		ImmutableSet<ClassInfo> topLevelClasses = ClassPath.from(
				contextClassLoader).getTopLevelClasses(packageName);
		for (ClassInfo classInfo : topLevelClasses) {
			Class<?> klass = classInfo.load();
			if (Controller.class.isAssignableFrom(klass)) {
				Class<? extends Controller> pagesClass = klass
						.asSubclass(Controller.class);
				this.registerClass(pagesClass);
			} else {
				logger.info("{} is not a Controller", klass);
			}
		}
	}

	public void registerClass(Class<? extends Controller> klass) {
		try {
			logger.info("Registering class: {}", klass);
			for (Method method : klass.getMethods()) {
				{
					POST post = method.getAnnotation(POST.class);
					if (post != null) {
						String path = post.value();
						Destination destination = new Destination(klass,
								method);
						logger.info("POST {}", path);
						router.post(path, destination);
					}
				}
				{
					GET get = method.getAnnotation(GET.class);
					if (get != null) {
						String path = get.value();
						Destination destination = new Destination(klass,
								method);
						logger.info("GET {}", path);
						router.get(path, destination);
					}
				}
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public void handler(HttpServletRequest request, HttpServletResponse response) {
		String method = request.getMethod();
		String path = request.getPathInfo();
		// log.debug("{} {}", method, path);
		RoutingResult<Destination> match = router.match(
				method, path);
		if (match == null) {
			this.writeNotFoundErrorPage(request, response);
			return;
		}

		if (!match.methodAllowed()) {
			this.writeMethodNotAllowedErrorPage(request, response);
			return;
		}

		try {
			Map<String, String> captured = match.getCaptured();
			Destination destination = match.getDestination();
			Controller pages = destination.getKlass().newInstance();
			pages.init(request, response, captured);
			pages.dispatch(destination.getMethod());
		} catch (IllegalAccessException | IllegalArgumentException
				| InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeMethodNotAllowedErrorPage(HttpServletRequest request,
			HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setStatus(405);
		response.setContentType("text/html; charset=utf-8");
		try {
			response.getWriter()
					.write("<!doctype html><html><div style='font-size: 400%'>405 Method Not Allowed</div></html>");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void writeNotFoundErrorPage(HttpServletRequest request,
			HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setStatus(404);
		response.setContentType("text/html; charset=utf-8");
		try {
			response.getWriter()
					.write("<!doctype html><html><div style='font-size: 400%'>404 Not Found</div></html>");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class Destination {
		private final Class<? extends Controller> klass;
		private final Method method;

		public Destination(Class<? extends Controller> klass,
				Method method) {
			this.klass = klass;
			this.method = method;
		}

		public Class<? extends Controller> getKlass() {
			return klass;
		}

		public Method getMethod() {
			return method;
		}
	}

	public WebRouter<Destination> getRouter() {
		return this.router;
	}

}
