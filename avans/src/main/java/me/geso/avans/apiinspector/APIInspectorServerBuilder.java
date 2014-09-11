package me.geso.avans.apiinspector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.ToString;
import me.geso.avans.Dispatcher;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.QueryParam;
import me.geso.routes.HttpRoute;
import me.geso.tinyvalidator.Constraint;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is a experimental feature. Do not use this.
 */
public class APIInspectorServerBuilder {
	private final int port;
	private final Dispatcher dispatcher;
	private int minThreads = 10;
	private int maxThreads = 10;
	private boolean accessLogEnabled = true;

	public APIInspectorServerBuilder(Dispatcher dispatcher, int port) {
		this.dispatcher = dispatcher;
		this.port = port;
	}

	public Server build() {
		QueuedThreadPool queuedThreadPool = new QueuedThreadPool(
				this.minThreads, this.maxThreads);
		Server server = new Server(queuedThreadPool);
		if (this.accessLogEnabled) {
			RequestLogHandler requestLogHandler = new RequestLogHandler();
			requestLogHandler.setRequestLog(new Slf4jRequestLog());
			server.setHandler(requestLogHandler);
		}

		ServerConnector serverConnector = new ServerConnector(server);
		serverConnector.setPort(port);
		server.addConnector(serverConnector);
		Servlet servlet = new APIInspectorServlet(this.dispatcher);
		ServletHolder servletHolder = new ServletHolder(servlet);
		ServletContextHandler context = new ServletContextHandler(
				server,
				"/",
				ServletContextHandler.SESSIONS
		);
		context.addServlet(servletHolder, "/*");
		server.setStopAtShutdown(true);
		return server;
	}

	public static class APIInspectorServlet extends HttpServlet {
		private final Dispatcher dispatcher;

		public APIInspectorServlet(Dispatcher dispatcher) {
			this.dispatcher = dispatcher;
		}

		public void service(HttpServletRequest req, HttpServletResponse res)
				throws ServletException, IOException {
			ObjectMapper mapper = new ObjectMapper();
			APIInformation info = new APIInformation(dispatcher);
			mapper.writeValue(res.getOutputStream(), info);
		}
	}

	@ToString
	public static class APIInformation {
		private final EndPoint[] endpoints;

		public APIInformation(Dispatcher dispatcher) {
			final List<HttpRoute<Dispatcher.Action>> patterns = dispatcher.getRouter().getPatterns();
			this.endpoints = patterns.stream()
					.map(route -> new EndPoint(
							route.getMethods(),
							route.getPathRoute().getPath(),
							route.getPathRoute().getDestination().getMethod())).toArray(EndPoint[]::new);
		}

		public APIClass[] getTypes() {
			APIClass[] apiClasses = Arrays.stream(this.endpoints).flatMap(
					endpoint -> {
						Type genericReturnType = endpoint.getMethod().getGenericReturnType();
						if (genericReturnType instanceof ParameterizedType) {
							List<Type> collect = Arrays.stream(((ParameterizedType) genericReturnType)
									.getActualTypeArguments()).collect(Collectors.toList());
							collect.add(((ParameterizedType) genericReturnType).getRawType());
							return collect.stream();
						} else {
							return Stream.of(genericReturnType);
						}
					}
			).distinct().filter(
					type -> type instanceof Class
			).map(
					type -> new APIClass((Class<?>) type)
			).toArray(APIClass[]::new);
			return apiClasses;
		}

		public EndPoint[] getEndpoints() {
			return endpoints;
		}

	}

	public static class APIClass {
		private final Class<?> klass;

		public APIClass(Class<?> klass) {
			this.klass = klass;
		}

		public String getName() {
			return this.klass.getName();
		}

		@SneakyThrows
		public APIProperty[] getFields() {
			BeanInfo beanInfo = Introspector.getBeanInfo(klass);
			return Arrays.stream(beanInfo.getPropertyDescriptors())
					.map(propertyDescriptor -> new APIProperty(propertyDescriptor))
					.toArray(APIProperty[]::new);
		}
	}

	public static class APIProperty {

		private final PropertyDescriptor propertyDescriptor;

		public APIProperty(final PropertyDescriptor propertyDescriptor) {
			this.propertyDescriptor = propertyDescriptor;
		}

		public String getType() {
			Method readMethod = this.propertyDescriptor.getReadMethod();
			if (readMethod != null) {
				return readMethod.getGenericReturnType().toString()
						.replaceAll("java\\.util\\.", "")
						.replaceAll("java\\.lang\\.", "");
			} else {
				return null;
			}
		}

		public String getName() {
			return this.propertyDescriptor.getName();
		}
	}

	@ToString
	public static class EndPoint {
		private final List<String> httpMethods;
		private final String path;

		@JsonIgnore
		public Method getMethod() {
			return method;
		}

		private final Method method;

		public EndPoint(List<String> httpMethods, String path,
						Method method) {
			this.httpMethods = httpMethods;
			this.method = method;
			this.path = path;
		}

		public String getReturnType() {
			final Type genericReturnType = method.getGenericReturnType();
			if (genericReturnType instanceof ParameterizedType) {
				StringBuilder buf = new StringBuilder();
				final Type rawType = ((ParameterizedType) genericReturnType).getRawType();
				if (rawType instanceof Class) {
					buf.append(((Class) rawType).getSimpleName());
				} else {
					buf.append(rawType.getTypeName());
				}
				buf.append("<");
				final String subtypes = Arrays.stream(((ParameterizedType) genericReturnType).getActualTypeArguments()).map(
						it -> {
							if (it instanceof Class) {
								return ((Class) it).getSimpleName();
							} else {
								return it.getTypeName();
							}
						}
				).collect(Collectors.joining(","));
				buf.append(subtypes);
				buf.append(">");
				return buf.toString();
			} else {
				return genericReturnType.toString();
			}
		}

		public List<String> getHttpMethod() {
			return httpMethods;
		}

		public String getPath() {
			return path;
		}

		public List<EndPointParameter> getParameters() {
			List<EndPointParameter> params = Arrays.stream(method.getParameters()).map(
					parameter -> new EndPointParameter(parameter)
			).collect(Collectors.toList());
			return Collections.unmodifiableList(params);
		}
	}

	@ToString
	public static class EndPointParameter {
		private final Parameter parameter;
		private String name;
		private String type;

		public EndPointParameter(Parameter parameter) {
			this.parameter = parameter;
			this.initialize();
			;
		}

		private final void initialize() {
			for (Annotation annotation : parameter.getAnnotations()) {
				if (annotation instanceof QueryParam) {
					this.name = ((QueryParam) annotation).value();
					this.type = "QueryParam";
					return;
				} else if (annotation instanceof PathParam) {
					this.name = ((PathParam) annotation).value();
					this.type = "PathParam";
					return;
				} else if (annotation instanceof BodyParam) {
					this.name = ((BodyParam) annotation).value();
					this.type = "BodyParam";
					return;
				} else if (annotation instanceof JsonParam) {
					this.name = null;
					this.type = "JsonParam";
					return;
				}
			}
			this.name = parameter.getName();
			this.type = null;
		}

		public String getType() {
			return this.type;
		}

		public String getName() {
			return this.name;
		}

		public List<EndPointConstraint> getConstraints() {
			List<EndPointConstraint> annotations = new ArrayList<>();
			for (Annotation annotation : parameter.getAnnotations()) {
				Constraint constraint = annotation.getClass().getAnnotation(Constraint.class);
				if (constraint != null) {
					annotations.add(new EndPointConstraint(annotation));
				}
			}
			return annotations;
		}
	}

	@ToString
	public static class EndPointConstraint {
		private Annotation annotation;

		public EndPointConstraint(Annotation annotation) {
			this.annotation = annotation;
		}

		@SneakyThrows
		public Object getValue() {
			Method method = this.annotation.annotationType().getMethod("value");
			if (method != null) {
				return method.invoke(annotation);
			} else {
				return null;
			}
		}

		public String getName() {
			return this.annotation.annotationType().getSimpleName();
		}
	}

}
