package me.geso.avans;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.BeanParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.Param;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.UploadFile;
import me.geso.avans.jackson.JacksonJsonParamReader;
import me.geso.avans.jackson.JacksonJsonView;
import me.geso.avans.trigger.ParamProcessor;
import me.geso.avans.trigger.ResponseConverter;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.RedirectResponse;
import me.geso.webscrew.response.WebResponse;

/**
 * You should create this object per HTTP request.
 *
 * @author tokuhirom
 */
@Slf4j
public abstract class ControllerBase implements Controller,
		JacksonJsonView, HTMLFilterProvider, JSONErrorPageRenderer,
		ValidatorProvider, TextRendererProvider, JacksonJsonParamReader {
	private HttpServletResponse servletResponse;
	private final Map<String, Object> pluginStash = new HashMap<>();
	private HttpServletRequest servletRequest;
	private Map<String, String> pathParams;
	private static final Logger logger = LoggerFactory
		.getLogger(ControllerBase.class);
	private static final Logger exceptionRootCauseLogger = LoggerFactory
		.getLogger("avans.exception.RootCause");
	private static final Logger exceptionStackTraceLogger = LoggerFactory
		.getLogger("avans.exception.StackTrace");

	@Override
	public void init(final HttpServletRequest servletRequest,
			final HttpServletResponse servletResponse,
			final Map<String, String> captured) {
		this.servletResponse = servletResponse;
		this.servletRequest = servletRequest;
		this.setDefaultCharacterEncoding();

		this.pathParams = Collections.unmodifiableMap(captured);
	}

	private void setDefaultCharacterEncoding() {
		this.servletResponse.setCharacterEncoding("UTF-8");
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return this.servletRequest;
	}

	/**
	 * Create new redirect response. You can use relative url here.
	 *
	 * @param location
	 * @return
	 */
	public RedirectResponse redirect(@NonNull final String location) {
		return new RedirectResponse(location);
	}

	/**
	 * Create new redirect response. You can use relative url here.
	 *
	 * @param location
	 * @return
	 */
	public RedirectResponse redirect(@NonNull final String location, @NonNull Map<String, String> parameters)
			throws URISyntaxException {
		final URIBuilder uriBuilder = new URIBuilder(location);
		parameters.entrySet().stream().forEach(
			e -> uriBuilder.setParameter(e.getKey(), e.getValue())
			);
		return new RedirectResponse(uriBuilder.build().toString());
	}

	/**
	 * Create new "405 Method Not Allowed" response in JSON.
	 *
	 * @return
	 */
	public WebResponse errorMethodNotAllowed() {
		return this.renderError(405, "Method Not Allowed");
	}

	/**
	 * Create new "403 Forbidden" response in JSON.
	 *
	 * @return
	 */
	public WebResponse errorForbidden() {
		return this.errorForbidden("Forbidden");
	}

	public WebResponse errorForbidden(final String message) {
		return this.renderError(403, message);
	}

	/**
	 * Create new "404 Not Found" response in JSON.
	 *
	 * @return
	 */
	public WebResponse errorNotFound() {
		return this.renderError(404, "Not Found");
	}

	/**
	 * Create new text/plain response.
	 *
	 * @param text
	 * @return
	 */
	@Override
	public WebResponse renderText(final String text) {
		if (text == null) {
			throw new IllegalArgumentException("text must not be null");
		}
		final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

		final ByteArrayResponse res = new ByteArrayResponse(200, bytes);
		res.setContentType("text/plain; charset=utf-8");
		return res;
	}

	@Override
	public String filterHTML(final String html) {
		String h = html;
		for (final Method filter : this.getFilters().getHtmlFilters()) {
			try {
				h = (String)filter.invoke(this, h);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return h;
	}

	@Override
	public void invoke(final Method method,
			final HttpServletRequest servletRequest,
			final HttpServletResponse servletResponse,
			final Map<String, String> captured) {
		try {
			this.init(servletRequest, servletResponse, captured);

			final WebResponse response = this.makeResponse(this, method);
			for (final Method filter : this.getFilters().getResponseFilters()) {
				filter.invoke(this, response);
			}
			response.write(servletResponse);
		} catch (final Throwable e) {
			final WebResponse response = this.handleException(e);
			try {
				response.write(servletResponse);
			} catch (final IOException ioe) {
				this.logException(ioe);
				throw new RuntimeException(ioe);
			}
		}
	}

	private void logException(Throwable e) {
		final Throwable root = this.unwrapRuntimeException(e);
		// Logging root cause in the log.
		{
			final StackTraceElement[] stackTrace = root.getStackTrace();
			if (stackTrace.length > 0) {
				final StackTraceElement ste = stackTrace[0];
				exceptionRootCauseLogger.error(
					"{}, {}, {}, {}, {}: {} at {}.{}({}:{})",
					this.servletRequest.getMethod(),
					this.servletRequest.getPathInfo(),
					this.servletRequest.getHeader("User-Agent"),
					this.servletRequest.getRemoteAddr(),
					root.getClass(),
					//
					root.getMessage(),
					ste.getClassName(),
					ste.getMethodName(),
					ste.getFileName(),
					ste.getLineNumber()
					);
			} else {
				exceptionRootCauseLogger.error("{}, {}, {}, {}, {}: {}",
					this.servletRequest.getMethod(),
					this.servletRequest.getPathInfo(),
					this.servletRequest.getHeader("User-Agent"),
					this.servletRequest.getRemoteAddr(),
					root.getClass(),
					//
					root.getMessage()
					);
			}
		}
		// Logging all messages in the fat log.
		{
			final StringWriter writer = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			final String s = writer.toString();

			exceptionStackTraceLogger.error("{}: {}\n{}", root.getClass(),
				root.getMessage(), s);
		}
	}

	// You can override me.
	public WebResponse handleException(Throwable e) {
		this.logException(e);
		return this.renderError(500, "Internal Server Error");
	}

	private Throwable unwrapRuntimeException(Throwable e) {
		while ((e instanceof RuntimeException || e instanceof InvocationTargetException)
			&& e.getCause() != null) {
			e = e.getCause();
		}
		return e;
	}

	final ConcurrentHashMap<Class<?>, Filters> filters = new ConcurrentHashMap<>();

	Filters getFilters() {
		return this.filters
			.computeIfAbsent(
				this.getClass(),
				(klass) -> {
					final FilterScanner scanner = new FilterScanner();
					scanner.scan(klass);
					return scanner.build();
				});
	}

	private WebResponse makeResponse(final Controller controller,
			final Method method) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException,
			ServletException, InstantiationException {
		for (final Method filter : this.getFilters()
			.getBeforeDispatchTriggers()) {
			try {
				@SuppressWarnings("unchecked")
				final Optional<WebResponse> webResponse = (Optional<WebResponse>)filter
					.invoke(this);
				if (webResponse == null) {
					throw new NullPointerException(
						"@BeforeDispatchTrigger shouldn't returned null. It should return `Optional<WebResponse>`.");
				}
				if (webResponse.isPresent()) {
					return webResponse.get();
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		final Parameter[] parameters = method.getParameters();
		final Object[] params = new Object[parameters.length];
		final List<String> missingParameters = new ArrayList<>();
		for (int i = 0; i < parameters.length; ++i) {
			final Parameter parameter = parameters[i];
			if (parameter.getAnnotation(BeanParam.class) != null) {
				// Process parameters annotated by @BeanParam
				final Field[] declaredFields = parameter.getType().getDeclaredFields();
				final Object bean = parameter.getType().newInstance();
				for (final Field field : declaredFields) {
					final ParameterProcessorResult value = this.getParameterValue(
						field, field.getType(), field.getName());
					if (value.hasResponse()) {
						return value.getResponse();
					} else if (value.hasData()) {
						field.setAccessible(true);
						field.set(bean, value.getData());
					} else {
						missingParameters.add(value.getMissingParameter());
					}
				}
				params[i] = bean;
			} else {
				final ParameterProcessorResult value = this
					.getParameterValue(parameter, parameter.getType(), parameter.getName());
				if (value.hasResponse()) {
					return value.getResponse();
				} else if (value.hasData()) {
					params[i] = value.getData();
				} else {
					missingParameters.add(value.getMissingParameter());
				}
			}
		}
		if (!missingParameters.isEmpty()) {
			return this.errorMissingMandatoryParameters(missingParameters);
		}
		final Optional<WebResponse> validationResult = this
			.validateParameters(method, params);
		if (validationResult.isPresent()) {
			return validationResult.get();
		}

		Object res;
		try {
			res = method.invoke(controller, params);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// It caused by programming error.
			logger.error("{}: {}: {}, {}", e, this.getServletRequest()
				.getPathInfo(),
				controller, params);
			throw new RuntimeException(e);
		}
		if (res instanceof WebResponse) {
			return (WebResponse)res;
		} else if (res == null) {
			throw new RuntimeException(
				"dispatch method must not return NULL");
		} else {
			for (final Method converter : this.getFilters()
				.getResponseConverters()) {
				final ResponseConverter annotation = converter
					.getAnnotation(ResponseConverter.class);
				if (res.getClass().isAssignableFrom(annotation.value())) {
					// Signature is : Optional<WebResponse> r(T o);
					final Object v = converter.invoke(this, res);
					if (v == null) {
						throw new NullPointerException(
							"@ResponseConverter must not return NULL");
					} else if (v instanceof Optional) {
						final Optional<?> ov = (Optional<?>)v;
						if (ov.isPresent()) {
							final WebResponse response = (WebResponse)ov.get();
							return response;
						} else {
							// Call next response converter.
							continue;
						}
					} else {
						throw new RuntimeException(
							"@ResponseConverter must return Optional<WebResponse>");
					}
				}
			}
			throw new RuntimeException(String.format(
				"Unknown return value from action: %s(%s)", res.getClass(),
				this.servletRequest.getPathInfo()));
		}
	}

	protected WebResponse errorMissingMandatoryParameters(
			List<String> missingParameters) {
		final int BAD_REQUEST = 400;
		final StringBuilder buf = new StringBuilder();
		buf.append("Missing mandatory parameter: ");
		buf.append(missingParameters.stream().collect(Collectors.joining(", ")));
		return this.renderError(BAD_REQUEST, new String(buf));
	}

	private <T> ParameterProcessorResult getParameterValue(
			final AnnotatedElement parameter, final Class<?> type, final String parameterName)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, ServletException,
			InstantiationException
	{
		// @ParamProcessor
		// public ParamProcessorResult paramUpperQ(Parameter parameter);
		for (final Method pp : this.getFilters().getParamProcessors()) {
			final ParamProcessor paramProcessor = pp
				.getAnnotation(ParamProcessor.class);

			if (paramProcessor.targetClass().isAssignableFrom(
				type)) {
				if (paramProcessor.targetAnnotation() == ParamProcessor.class
					|| parameter.getAnnotation(paramProcessor
						.targetAnnotation()) != null) {
					final Object result = pp.invoke(this, parameter);
					if (result == null) {
						throw new NullPointerException(
							"@ParamProcessor returns null: "
								+ pp);
					} else if (result instanceof ParameterProcessorResult) {
						if (((ParameterProcessorResult)result).hasData()
							|| ((ParameterProcessorResult)result)
								.hasResponse()) {
							return (ParameterProcessorResult)result;
						}
					} else {
						throw new RuntimeException(
							"@ParamProcessor should return ParameterProcessorResult, but "
								+ pp);
					}
				}
			}
		}

		final Annotation[] annotations = parameter.getAnnotations();
		for (final Annotation annotation : annotations) {
			if (annotation instanceof JsonParam) {
				final InputStream is = this.servletRequest.getInputStream();
				final Object value = this.readJsonParam(is, type);
				return ParameterProcessorResult.fromData(value);
			} else if (annotation instanceof Param) {
				final String name = ((Param)annotation).value();
				final String value = this.getServletRequest()
					.getParameter(name);
				return this.getObjectFromParameterObjectValue(annotation, name,
					type, value);
			} else if (annotation instanceof PathParam) {
				final String name = ((PathParam)annotation).value();
				final String value = this.pathParams.get(name);
				return this.getObjectFromParameterObjectValue(annotation, name,
					type, value);
			} else if (annotation instanceof UploadFile) {
				// @UploadFile
				final String name = ((UploadFile)annotation).value();
				if (type == Part.class) {
					final Part part = this.servletRequest.getPart(name);
					if (part != null) {
						return ParameterProcessorResult.fromData(part);
					} else {
						return ParameterProcessorResult.missingParameter(name);
					}
				} else if (type == Part[].class) {
					final Part[] parts = this.servletRequest.getParts()
						.stream().filter(part -> {
							return name.equals(part.getName());
						}).toArray(Part[]::new);
					return ParameterProcessorResult.fromData(parts);
				} else if (type == Optional.class) {
					// It must be Optional<WebRequestUpload>
					// TODO: support Optional<Part>
					try {
						final Part part = this.servletRequest.getPart(name);
						if (part != null) {
							return ParameterProcessorResult
								.fromData(
								Optional.of(part));
						} else {
							return ParameterProcessorResult.fromData(Optional
								.empty());
						}
					} catch (final IOException e) {
						// We must catch this exception.
						// Since jetty throws exception if the request doesn't
						// have a part.
						// See
						// org.eclipse.jetty.util.MultiPartInputStreamParser.parse.
						log.info("{}: {}", e.getClass(), e.getMessage());
						return ParameterProcessorResult.fromData(Optional
							.empty());
					}
				} else {
					throw new RuntimeException(
						String.format(
							"You shouldn't use @UploadFile annotation with %s. You must use Part or Part[]",
							type));
				}
			}
		}

		// Programming error. You may forget to specify the annotation.
		throw new RuntimeException(String.format(
			"There is no way to create parameter: %s, %s, %s",
			this.getClass().getName(), this.getServletRequest()
				.getPathInfo(),
			parameterName));
	}

	private ParameterProcessorResult getObjectFromParameterObjectValue(
			final Annotation annotation,
			final String name,
			final Class<?> type,
			final String value) {
		if (type.equals(String.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(value);
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(int.class) || type.equals(Integer.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(Integer
					.parseInt(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(long.class) || type.equals(Long.class)) {
			if (value != null) {
				return ParameterProcessorResult
					.fromData(Long.parseLong(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(short.class) || type.equals(Short.class)) {
			if (value != null) {
				return ParameterProcessorResult
					.fromData(Short.parseShort(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(double.class) || type.equals(Double.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(Double
					.parseDouble(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(
					Boolean.parseBoolean(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(OptionalInt.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(OptionalInt.of(Integer
					.parseInt(value)));
			} else {
				return ParameterProcessorResult.fromData(OptionalInt.empty());
			}
		} else if (type.equals(OptionalLong.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(OptionalLong.of(Long
					.parseLong(value)));
			} else {
				return ParameterProcessorResult.fromData(OptionalLong.empty());
			}
		} else if (type.equals(OptionalDouble.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(OptionalDouble
					.of(Double
						.parseDouble(value)));
			} else {
				return ParameterProcessorResult
					.fromData(OptionalDouble.empty());
			}
		} else if (type.equals(Optional.class)) {
			// avans supports Optional<String> only.
			// TODO: type parameter check
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(Optional.of(value));
			} else {
				return ParameterProcessorResult.fromData(Optional.empty());
			}
		} else {
			// Programming error
			throw new RuntimeException(String.format(
				"Unknown parameter type '%s' for '%s'", type, name));
		}
	}

	@Override
	public void close() {
	}

	private String generatePluginStashKey(Class<?> pluginClass, String key) {
		return pluginClass.getName() + "#" + key;
	}

	@Override
	public Optional<Object> getPluginStashValue(Class<?> pluginClass, String key) {
		final Object object = this.pluginStash.get(this.generatePluginStashKey(
			pluginClass, key));
		return Optional.ofNullable(object);
	}

	@Override
	public void setPluginStashValue(Class<?> pluginClass, String key,
			Object value) {
		this.pluginStash.put(this.generatePluginStashKey(pluginClass, key),
			value);
	}

	@Override
	public Object computePluginStashValueIfAbsent(Class<?> pluginClass,
			String key,
			Supplier<?> supplier) {
		return this.pluginStash.computeIfAbsent(
			this.generatePluginStashKey(pluginClass, key),
			(fullKey) -> supplier.get());
	}

	@Override
	public Map<String, String> getPathParams() {
		return this.pathParams;
	}

}
