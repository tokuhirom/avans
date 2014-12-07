package me.geso.avans;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.Param;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.QueryParam;
import me.geso.avans.annotation.UploadFile;
import me.geso.avans.jackson.JacksonJsonParamReader;
import me.geso.avans.jackson.JacksonJsonView;
import me.geso.avans.trigger.ParamProcessor;
import me.geso.avans.trigger.ResponseConverter;
import me.geso.webscrew.Parameters;
import me.geso.webscrew.request.WebRequest;
import me.geso.webscrew.request.WebRequestUpload;
import me.geso.webscrew.request.impl.DefaultParameters;
import me.geso.webscrew.request.impl.DefaultParameters.Builder;
import me.geso.webscrew.request.impl.DefaultWebRequest;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.RedirectResponse;
import me.geso.webscrew.response.WebResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * You should create this object per HTTP request.
 *
 * @author tokuhirom
 */
@SuppressWarnings("deprecation")
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

	@Deprecated
	public WebRequest createWebReqeust(final HttpServletRequest servletRequest) {
		try {
			return new DefaultWebRequest(servletRequest, StandardCharsets.UTF_8);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private void setDefaultCharacterEncoding() {
		this.servletResponse.setCharacterEncoding("UTF-8");
	}

	@Override
	@Deprecated
	public WebRequest getRequest() {
		return this.createWebReqeust(this.servletRequest);
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return this.servletRequest;
	}

	/**
	 * Get a path parameter.
	 *
	 * @return
	 */
	@Deprecated
	@Override
	public Parameters getPathParameters() {
		final Builder pathParameters = DefaultParameters.builder();
		for (final Entry<String, String> entry : this.pathParams.entrySet()) {
			pathParameters.put(entry.getKey(), entry.getValue());
		}
		return pathParameters.build();
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
				h = (String) filter.invoke(this, h);
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
			ServletException {
		for (final Method filter : this.getFilters()
				.getBeforeDispatchTriggers()) {
			try {
				@SuppressWarnings("unchecked")
				final Optional<WebResponse> webResponse = (Optional<WebResponse>) filter
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
			final ParameterProcessorResult value = this
					.getParameterValue(parameter);
			if (value.hasResponse()) {
				return value.getResponse();
			} else if (value.hasData()) {
				params[i] = value.getData();
			} else {
				missingParameters.add(value.getMissingParameter());
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
			return (WebResponse) res;
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
						final Optional<?> ov = (Optional<?>) v;
						if (ov.isPresent()) {
							final WebResponse response = (WebResponse) ov.get();
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
			final Parameter parameter)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, ServletException
	{
		// @ParamProcessor
		// public ParamProcessorResult paramUpperQ(Parameter parameter);
		for (final Method pp : this.getFilters().getParamProcessors()) {
			final ParamProcessor paramProcessor = pp
					.getAnnotation(ParamProcessor.class);

			if (paramProcessor.targetClass().isAssignableFrom(
					parameter.getType())) {
				if (paramProcessor.targetAnnotation() == ParamProcessor.class
						|| parameter.getAnnotation(paramProcessor
								.targetAnnotation()) != null) {
					final Object result = pp.invoke(this, parameter);
					if (result == null) {
						throw new NullPointerException(
								"@ParamProcessor returns null: "
										+ pp);
					} else if (result instanceof ParameterProcessorResult) {
						if (((ParameterProcessorResult) result).hasData()
								|| ((ParameterProcessorResult) result)
										.hasResponse()) {
							return (ParameterProcessorResult) result;
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
		final Class<?> type = parameter.getType();
		for (final Annotation annotation : annotations) {
			if (annotation instanceof JsonParam) {
				final InputStream is = this.servletRequest.getInputStream();
				final Object value = this.readJsonParam(is, type);
				return ParameterProcessorResult.fromData(value);
			} else if (annotation instanceof Param) {
				final String name = ((Param) annotation).value();
				final String value = this.getServletRequest()
						.getParameter(name);
				return this.getObjectFromParameterObjectValue(annotation, name,
						type, value);
			} else if (annotation instanceof QueryParam) {
				final String name = ((QueryParam) annotation).value();
				final String value = this.getServletRequest()
						.getParameter(name);
				return this.getObjectFromParameterObjectValue(annotation, name,
						type, value);
			} else if (annotation instanceof BodyParam) {
				final String name = ((BodyParam) annotation).value();
				final String value = this.getServletRequest()
						.getParameter(name);
				return this.getObjectFromParameterObjectValue(annotation, name,
						type, value);
			} else if (annotation instanceof PathParam) {
				final String name = ((PathParam) annotation).value();
				final String value = this.pathParams.get(name);
				return this.getObjectFromParameterObjectValue(annotation, name,
						type, value);
			} else if (annotation instanceof UploadFile) {
				// @UploadFile
				final String name = ((UploadFile) annotation).value();
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
				} else if (type == WebRequestUpload.class) {
					// TODO: remove me
					final Part part = this.servletRequest.getPart(name);
					if (part != null) {
						return ParameterProcessorResult
								.fromData(new PartWebRequestUpload(part));
					} else {
						return ParameterProcessorResult.missingParameter(name);
					}
				} else if (type == WebRequestUpload[].class) {
					// TODO: remove me
					final WebRequestUpload[] parts = this.servletRequest
							.getParts()
							.stream()
							.filter(part -> name.equals(part.getName()))
							.map(part -> new PartWebRequestUpload(part))
							.toArray(WebRequestUpload[]::new);
					return ParameterProcessorResult.fromData(parts);
				} else if (type == Optional.class) {
					// It must be Optional<WebRequestUpload>
					// TODO: support Optional<Part>
					try {
						final Part part = this.servletRequest.getPart(name);
						if (part != null) {
							return ParameterProcessorResult
									.fromData(
									Optional.of(new PartWebRequestUpload(part)));
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
				parameter.getName()));
	}

	/**
	 * Uploaded file object built on Servlet 3.0's javax.servlet.http.Part object.
	 */
	class PartWebRequestUpload implements WebRequestUpload {
		private final Part part;

		public PartWebRequestUpload(Part part) {
			this.part = part;
		}

		@Override
		public String getString(String encoding) {
			try {
				final String string = IOUtils.toString(
						this.part.getInputStream(),
						encoding);
				return string;
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public InputStream getInputStream() {
			try {
				return this.part.getInputStream();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getName() {
			return this.part.getName();
		}

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
		} else if (type.equals(int.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(Integer
						.parseInt(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(long.class)) {
			if (value != null) {
				return ParameterProcessorResult
						.fromData(Long.parseLong(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(double.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(Double
						.parseDouble(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(OptionalInt.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(OptionalInt.of(Integer
						.parseInt(value)));
			} else {
				return ParameterProcessorResult.fromData(OptionalInt.empty());
			}
		} else if (type.equals(OptionalLong.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(OptionalLong.of(Long
						.parseLong(value)));
			} else {
				return ParameterProcessorResult.fromData(OptionalLong.empty());
			}
		} else if (type.equals(OptionalDouble.class)) {
			if (value != null) {
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
			if (value != null) {
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
