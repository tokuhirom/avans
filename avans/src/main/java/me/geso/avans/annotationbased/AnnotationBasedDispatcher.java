package me.geso.avans.annotationbased;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import me.geso.avans.AvansDispatcher;
import me.geso.avans.AvansRequest;
import me.geso.avans.AvansResponse;
import me.geso.avans.AvansWebApplication;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class AnnotationBasedDispatcher implements AvansDispatcher {
	private final WebRouter<Method> router = new WebRouter<>();

	public AnnotationBasedDispatcher() {
	}

	public void registerPackage(String packageName) throws IOException {
		ClassLoader contextClassLoader = Thread.currentThread()
				.getContextClassLoader();
		ImmutableSet<ClassInfo> topLevelClasses = ClassPath.from(
				contextClassLoader).getTopLevelClasses(packageName);
		for (ClassInfo classInfo : topLevelClasses) {
			Class<?> klass = classInfo.load();
			this.registerClass(klass);
		}
	}

	public void registerClass(Class<?> klass) {
		for (Method method : klass.getMethods()) {
			{
				POST post = method.getAnnotation(POST.class);
				if (post != null) {
					String path = post.value();
					router.post(path, method);
				}
			}
			{
				GET get = method.getAnnotation(GET.class);
				if (get != null) {
					String path = get.value();
					router.get(path, method);
				}
			}
		}
	}

	public AvansResponse dispatch(AvansWebApplication web) {
		AvansRequest request = web.getRequest();
		String method = request.getMethod();
		String path = request.getPathInfo();
		// log.debug("{} {}", method, path);
		RoutingResult<Method> match = router.match(
				method, path);
		if (match == null) {
			return web.errorNotFound();
		}

		if (!match.methodAllowed()) {
			return web.errorMethodNotAllowed();
		}

		Map<String, String> captured = match.getCaptured();
		web.setPathParameters(captured);
		Method destination = match.getDestination();
		try {
			AvansResponse response = (AvansResponse) destination.invoke(null, web);
			if (response == null) {
				throw new RuntimeException(String.format(
						"Response must not be null: %s, %s, %s",
						request.getMethod(), request.getPathInfo(),
						destination.toString()
						));
			}
			return response;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
