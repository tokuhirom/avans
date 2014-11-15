package me.geso.avans;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.QueryParam;
import me.geso.avans.annotation.UploadFile;
import me.geso.avans.jackson.JacksonJsonView;
import me.geso.avans.trigger.ParamProcessor;
import me.geso.avans.trigger.ResponseConverter;
import me.geso.tinyvalidator.ConstraintViolation;
import me.geso.tinyvalidator.Validator;
import me.geso.webscrew.Parameters;
import me.geso.webscrew.request.WebRequest;
import me.geso.webscrew.request.WebRequestUpload;
import me.geso.webscrew.request.impl.DefaultParameters;
import me.geso.webscrew.request.impl.DefaultParameters.Builder;
import me.geso.webscrew.request.impl.DefaultWebRequest;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.RedirectResponse;
import me.geso.webscrew.response.WebResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * You should create this object per HTTP request.
 *
 * @author tokuhirom
 */
public abstract class ControllerBase implements Controller,
		JacksonJsonView, HTMLFilterProvider, JSONErrorPageRenderer {
	private WebRequest request;
	private HttpServletResponse servletResponse;
	private Parameters pathParameters;
	private final Map<String, Object> pluginStash = new HashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
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
		this.request = this.createWebReqeust(servletRequest);
		this.servletResponse = servletResponse;
		this.setDefaultCharacterEncoding();

		final Builder pathParameters = DefaultParameters.builder();
		for (final Entry<String, String> entry : captured.entrySet()) {
			pathParameters.put(entry.getKey(), entry.getValue());
		}
		this.pathParameters = pathParameters.build();
	}

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
	public WebRequest getRequest() {
		return this.request;
	}

	/**
	 * Get a path parameter.
	 *
	 * @return
	 */
	@Override
	public Parameters getPathParameters() {
		return this.pathParameters;
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
						this.getRequest().getMethod(),
						this.getRequest().getPathInfo(),
						this.getRequest().getUserAgent(),
						this.getRequest().getRemoteAddr(),
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
						this.getRequest().getMethod(),
						this.getRequest().getPathInfo(),
						this.getRequest().getUserAgent(),
						this.getRequest().getRemoteAddr(),
						root.getClass(),
						//
						root.getMessage()
						);
			}
		}
		// Logging all messages in the fat log.
		exceptionStackTraceLogger.error("{}, {}\n{}", e.getCause(),
				e.getMessage(), e.getStackTrace());
		e.printStackTrace();
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
			IllegalArgumentException, InvocationTargetException {
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
		final List<String> violationMessages = new ArrayList<>();
		for (int i = 0; i < parameters.length; ++i) {
			final Parameter parameter = parameters[i];
			final ParameterProcessorResult value = this
					.getParameterValue(parameter);
			if (value.hasResponse()) {
				return value.getResponse();
			} else if (value.hasData()) {
				this.validateParameter(parameter, value.getData(),
						violationMessages);
				params[i] = value.getData();
			} else {
				violationMessages.add(String.format(
						"Missing mandatory parameter: %s",
						value.getMissingParameter()));
			}
		}
		if (!violationMessages.isEmpty()) {
			return this.renderValidationFailedResponse(violationMessages);
		}

		Object res;
		try {
			res = method.invoke(controller, params);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// It caused by programming error.
			logger.error("{}: {}: {}, {}", e, this.request.getPathInfo(),
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
					this.getRequest().getPathInfo()));
		}
	}

	private WebResponse renderValidationFailedResponse(
			final List<String> violationMessages) {
		return this.renderJSON(new BasicAPIResponse(403, violationMessages));
	}

	protected void validateParameter(final Parameter parameter,
			final Object value,
			final List<String> violationMessages) {
		final Validator validator = new Validator();
		final Annotation[] annotations = parameter.getAnnotations();
		for (final Annotation annotation : annotations) {
			if (annotation instanceof JsonParam) {
				final List<ConstraintViolation> validate = validator
						.validate(value);
				validate.stream().forEach(
						violation -> {
							final String message = violation.getName() + " "
									+ violation.getMessage();
							violationMessages.add(message);
						}
						);
			} else {
				final Optional<ConstraintViolation> constraintViolationOptional = validator
						.validateByAnnotation(annotation, parameter.getName(),
								value);
				if (constraintViolationOptional.isPresent()) {
					final ConstraintViolation constraintViolation = constraintViolationOptional
							.get();
					violationMessages.add(constraintViolation.getName() + " "
							+ constraintViolation.getMessage());
				}
			}
		}
	}

	private ParameterProcessorResult getParameterValue(
			final Parameter parameter)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException
	{
		final Optional<Object> objectOptional = this.GET_PARAMETER(parameter);
		if (objectOptional.isPresent()) {
			return ParameterProcessorResult.fromData(objectOptional.get());
		}

		// public ParamProcessorResult paramUpperQ(Parameter parameter);
		for (final Method pp : this.getFilters().getParamProcessors()) {
			final ParamProcessor paramProcessor = pp
					.getAnnotation(ParamProcessor.class);
			if (parameter.getType().isAssignableFrom(
					paramProcessor.targetClass())) {
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
					throw new NullPointerException(
							"@ParamProcessor returns null: "
									+ pp);
				}
			}
		}

		final Annotation[] annotations = parameter.getAnnotations();
		final Class<?> type = parameter.getType();
		for (final Annotation annotation : annotations) {
			if (annotation instanceof JsonParam) {
				try {
					final InputStream is = this.getRequest().getInputStream();
					final Object value = this.objectMapper.readValue(is, type);
					return ParameterProcessorResult.fromData(value);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			} else if (annotation instanceof QueryParam) {
				final String name = ((QueryParam) annotation).value();
				return this.getObjectFromParameterObject(annotation, name,
						type,
						this.getRequest()
								.getQueryParams());
			} else if (annotation instanceof BodyParam) {
				final String name = ((BodyParam) annotation).value();
				return this.getObjectFromParameterObject(annotation, name,
						type,
						this.getRequest()
								.getBodyParams());
			} else if (annotation instanceof PathParam) {
				final String name = ((PathParam) annotation).value();
				return this.getObjectFromParameterObject(annotation, name,
						type,
						this.getPathParameters());
			} else if (annotation instanceof UploadFile) {
				// @UploadFile
				final String name = ((UploadFile) annotation).value();
				if (type == WebRequestUpload.class) {
					final Optional<WebRequestUpload> maybeFileItem = this
							.getRequest()
							.getFirstFileItem(name);
					return ParameterProcessorResult.fromData(maybeFileItem
							.get());
				} else if (type == WebRequestUpload[].class) {
					final WebRequestUpload[] items = this.getRequest()
							.getAllFileItems(name)
							.toArray(new WebRequestUpload[0]);
					return ParameterProcessorResult.fromData(items);
				} else if (type == Optional.class) {
					// It must be Optional<FileItem>
					final Optional<WebRequestUpload> maybeFileItem = this
							.getRequest()
							.getFirstFileItem(name);
					return ParameterProcessorResult.fromData(maybeFileItem);
				} else {
					throw new RuntimeException(
							String.format(
									"You shouldn't use @UploadFile annotation with %s. You must use FileItem or FileItem[]",
									type));
				}
			}
		}

		// Programming error. You may forget to specify the annotation.
		throw new RuntimeException(String.format(
				"There is no way to create parameter: %s, %s, %s",
				this.getClass().getName(), this.getRequest().getPathInfo(),
				parameter.getName()));
	}

	private ParameterProcessorResult getObjectFromParameterObject(
			final Annotation annotation,
			final String name,
			final Class<?> type,
			final Parameters params) {
		if (type.equals(String.class)) {
			final Optional<String> value = params.getFirst(name);
			if (!value.isPresent()) {
				return ParameterProcessorResult.missingParameter(name);
			}
			return ParameterProcessorResult.fromData(value.get());
		} else if (type.equals(int.class)) {
			final Optional<String> value = params.getFirst(name);
			if (!value.isPresent()) {
				return ParameterProcessorResult.missingParameter(name);
			}
			return ParameterProcessorResult.fromData(Integer.parseInt(value
					.get()));
		} else if (type.equals(long.class)) {
			final Optional<String> value = params.getFirst(name);
			if (!value.isPresent()) {
				return ParameterProcessorResult.missingParameter(name);
			}
			return ParameterProcessorResult
					.fromData(Long.parseLong(value.get()));
		} else if (type.equals(double.class)) {
			final Optional<String> value = params.getFirst(name);
			if (!value.isPresent()) {
				return ParameterProcessorResult.missingParameter(name);
			}
			return ParameterProcessorResult.fromData(Double.parseDouble(value
					.get()));
		} else if (type.equals(OptionalInt.class)) {
			final Optional<String> value = params.getFirst(name);
			if (value.isPresent()) {
				return ParameterProcessorResult.fromData(OptionalInt.of(Integer
						.parseInt(value.get())));
			} else {
				return ParameterProcessorResult.fromData(OptionalInt.empty());
			}
		} else if (type.equals(OptionalLong.class)) {
			final Optional<String> value = params.getFirst(name);
			if (value.isPresent()) {
				return ParameterProcessorResult.fromData(OptionalLong.of(Long
						.parseLong(value.get())));
			} else {
				return ParameterProcessorResult.fromData(OptionalLong.empty());
			}
		} else if (type.equals(OptionalDouble.class)) {
			final Optional<String> value = params.getFirst(name);
			if (value.isPresent()) {
				return ParameterProcessorResult.fromData(OptionalDouble
						.of(Double
								.parseDouble(value
										.get())));
			} else {
				return ParameterProcessorResult
						.fromData(OptionalDouble.empty());
			}
		} else if (type.equals(Optional.class)) {
			// avans supports Optional<String> only.
			// TODO: type parameter check
			final Optional<String> value = params.getFirst(name);
			if (value.isPresent()) {
				return ParameterProcessorResult.fromData(Optional.of(value
						.get()));
			} else {
				return ParameterProcessorResult.fromData(Optional.empty());
			}
		} else {
			// Programming error
			throw new RuntimeException(String.format(
					"Unknown parameter type '%s' for '%s'", type, name));
		}
	}

	// DEPRECATED.
	protected Optional<Object> GET_PARAMETER(final Parameter parameter) {
		return Optional.empty();
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

}
