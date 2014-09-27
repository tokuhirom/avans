package me.geso.avans;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.QueryParam;
import me.geso.avans.annotation.UploadFile;
import me.geso.avans.jackson.JacksonJsonView;
import me.geso.avans.trigger.BeforeDispatchTrigger;
import me.geso.avans.trigger.HTMLFilter;
import me.geso.avans.trigger.ResponseFilter;
import me.geso.tinyvalidator.ConstraintViolation;
import me.geso.tinyvalidator.Validator;
import me.geso.webscrew.Parameters;
import me.geso.webscrew.request.WebRequest;
import me.geso.webscrew.request.WebRequestUpload;
import me.geso.webscrew.request.impl.DefaultWebRequest;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.RedirectResponse;
import me.geso.webscrew.response.WebResponse;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

/**
 * You should create this object per HTTP request.
 *
 * @author tokuhirom
 */
public abstract class ControllerBase implements Controller,
JacksonJsonView, HTMLFilterProvider {
	private WebRequest request;
	private HttpServletResponse servletResponse;
	private Parameters pathParameters;
	private final Map<String, Object> pluginStash = new HashMap<>();

	@Override
	public void init(final HttpServletRequest servletRequest,
			final HttpServletResponse servletResponse,
			final Map<String, String> captured) {
		this.BEFORE_INIT();
		this.request = this.createWebReqeust(servletRequest);
		this.servletResponse = servletResponse;
		this.setDefaultCharacterEncoding();

		final MultiMap<String, String> pathParameters = new MultiValueMap<String, String>();
		for (final Entry<String, String> entry : captured.entrySet()) {
			pathParameters.put(entry.getKey(), entry.getValue());
		}
		this.pathParameters = new Parameters(pathParameters);
		this.AFTER_INIT();
	}

	public WebRequest createWebReqeust(final HttpServletRequest servletRequest) {
		return new DefaultWebRequest(servletRequest);
	}

	protected void BEFORE_INIT() {
		// I am hook point.
	}

	protected void AFTER_INIT() {
		// I am hook point.
	}

	/**
	 * Normally, you shouldn't use this directly... For shooting itself in the
	 * foot.
	 *
	 * @return
	 */
	protected HttpServletResponse getServletResponse() {
		return this.servletResponse;
	}

	public void setDefaultCharacterEncoding() {
		this.servletResponse.setCharacterEncoding("UTF-8");
		this.request.setCharacterEncoding("UTF-8");
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
	 * Render the error response.
	 *
	 * @param code
	 * @param message
	 * @return
	 */
	protected WebResponse renderError(final int code,
			@NonNull final String message) {
		final APIResponse<String> apires = new APIResponse<>(code, message,
				null);

		final WebResponse res = this.renderJSON(code, apires);
		return res;
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

		final ByteArrayResponse res = new ByteArrayResponse();
		res.setContentType("text/plain; charset=utf-8");
		res.setBody(bytes);
		return res;
	}

	@Override
	public String filterHTML(String html) {
		for (final Method filter : this.getFilters().getHtmlFilters()) {
			try {
				html = (String) filter.invoke(this, html);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return html;
	}

	/**
	 * Get project base directory. TODO: better jar location detection
	 * algorithm.
	 *
	 * @return
	 */
	@Override
	public Path getBaseDirectory() {
		return AvansUtil.getBaseDirectory(this.getClass());
	}

	/**
	 * This is a hook point. You can override this.<br>
	 * <br>
	 * Use case:
	 * <ul>
	 * <li>Authentication before dispatching</li>
	 * </ul>
	 *
	 * @return
	 */
	protected Optional<WebResponse> BEFORE_DISPATCH() {
		// override me.
		return Optional.empty();
	}

	@Override
	public void invoke(final Method method,
			final HttpServletRequest servletRequest,
			final HttpServletResponse servletResponse,
			final Map<String, String> captured) {
		this.init(servletRequest, servletResponse, captured);

		final WebResponse response = this.makeResponse(this, method);
		for (final Method filter : this.getFilters().getResponseFilters()) {
			try {
				filter.invoke(this, response);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			response.write(servletResponse);
		} catch (final IOException e) {
			// User can't recovery this exception.
			throw new RuntimeException(e);
		}
	}

	final ConcurrentHashMap<Class<?>, Filters> responseFilters = new ConcurrentHashMap<>();

	private Filters getFilters() {
		return this.responseFilters
				.computeIfAbsent(
						this.getClass(),
						(klass) -> {
							// LinkedList じゃなくてもっとうまいやり方あると思う｡
							final LinkedList<Class<?>> linearIsa = new LinkedList<>();
							while (klass != null
									&& klass != ControllerBase.class) {
								linearIsa.addFirst(klass);
								klass = klass.getSuperclass();
							}
							final List<Method> responseFilters = new ArrayList<>();
							final List<Method> htmlFilters = new ArrayList<>();
							final List<Method> beforeDispatchTriggers = new ArrayList<>();
							for (final Class<?> k : linearIsa) {
								for (final Class<?> interfac : k
										.getInterfaces()) {
									for (final Method method : interfac
											.getMethods()) {
										if (method
												.getAnnotation(BeforeDispatchTrigger.class) != null) {
											beforeDispatchTriggers.add(method);
										}
										if (method
												.getAnnotation(HTMLFilter.class) != null) {
											htmlFilters.add(method);
										}
										if (method
												.getAnnotation(ResponseFilter.class) != null) {
											responseFilters.add(method);
										}
									}
								}
							}
							return new Filters(
									beforeDispatchTriggers,
									htmlFilters,
									responseFilters
									);
						});
	}

	private static class Filters {
		private final List<Method> responseFilters;
		private final List<Method> beforeDispatchTriggers;
		private final List<Method> htmlFilters;

		public Filters(
				final List<Method> beforeDispatchTriggers,
				final List<Method> htmlFilters,
				final List<Method> responseFilters) {
			this.responseFilters = responseFilters;
			this.beforeDispatchTriggers = beforeDispatchTriggers;
			this.htmlFilters = htmlFilters;
		}

		public List<Method> getResponseFilters() {
			return this.responseFilters;
		}

		public List<Method> getBeforeDispatchTriggers() {
			return this.beforeDispatchTriggers;
		}

		public List<Method> getHtmlFilters() {
			return this.htmlFilters;
		}

	}

	private WebResponse makeResponse(final Controller controller,
			final Method method) {
		{
			final Optional<WebResponse> maybeResponse = this.BEFORE_DISPATCH();
			if (maybeResponse.isPresent()) {
				return maybeResponse.get();
			}
		}

		for (final Method filter : this.getFilters()
				.getBeforeDispatchTriggers()) {
			try {
				@SuppressWarnings("unchecked")
				final Optional<WebResponse> webResponse = (Optional<WebResponse>) filter
				.invoke(this);
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
			final Object value = this.getParameterValue(parameter);
			this.validateParameter(parameter, value, violationMessages);
			params[i] = value;
		}
		if (!violationMessages.isEmpty()) {
			return this.errorValidationFailed(violationMessages);
		}

		Object res;
		try {
			res = method.invoke(controller, params);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// It caused by programming error.
			throw new RuntimeException(e);
		}
		if (res instanceof WebResponse) {
			return (WebResponse) res;
		} else if (res == null) {
			throw new RuntimeException(
					"dispatch method must not return NULL");
		} else {
			return this.convertResponse(controller, res);
		}
	}

	private WebResponse errorValidationFailed(
			final List<String> violationMessages) {
		return this.renderJSON(new APIResponse<>(403, violationMessages, null));
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

	private Object getParameterValue(final Parameter parameter) {
		final Optional<Object> objectOptional = this.GET_PARAMETER(parameter);
		if (objectOptional.isPresent()) {
			return objectOptional.get();
		}

		final Annotation[] annotations = parameter.getAnnotations();
		final Class<?> type = parameter.getType();
		for (final Annotation annotation : annotations) {
			if (annotation instanceof JsonParam) {
				final Object value = this.getRequest().readJSON(type);
				return value;
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
							.getFileItem(name);
					if (maybeFileItem.isPresent()) {
						return maybeFileItem.get();
					} else {
						throw new RuntimeException(String.format(
								"Missing mandatory file: %s", name));
					}
				} else if (type == WebRequestUpload[].class) {
					final WebRequestUpload[] items = this.getRequest()
							.getFileItems(name)
							.toArray(new WebRequestUpload[0]);
					return items;
				} else if (type == Optional.class) {
					// It must be Optional<FileItem>
					final Optional<WebRequestUpload> maybeFileItem = this
							.getRequest()
							.getFileItem(name);
					return maybeFileItem;
				} else {
					throw new RuntimeException(
							String.format(
									"You shouldn't use @UploadFile annotation with %s. You must use FileItem or FileItem[]",
									type));
				}
			}
		}

		throw new RuntimeException(String.format(
				"There is no way to create parameter: %s, %s, %s",
				this.getClass().getName(), this.getRequest().getPathInfo(),
				parameter.getName()));
	}

	private Object getObjectFromParameterObject(final Annotation annotation,
			final String name,
			final Class<?> type,
			final Parameters params) {
		if (type.equals(String.class)) {
			if (!params.containsKey(name)) {
				throw new RuntimeException(String.format(
						"Missing mandatory parameter '%s' by %s", name,
						annotation.getClass().getName()));
			}
			return params.get(name);
		} else if (type.equals(int.class)) {
			if (!params.containsKey(name)) {
				throw new RuntimeException(String.format(
						"Missing mandatory parameter '%s' by %s", name,
						annotation.getClass().getName()));
			}
			return params.getInt(name);
		} else if (type.equals(long.class)) {
			if (!params.containsKey(name)) {
				throw new RuntimeException(String.format(
						"Missing mandatory parameter '%s' by %s", name,
						annotation.getClass().getName()));
			}
			return params.getLong(name);
		} else if (type.equals(double.class)) {
			if (!params.containsKey(name)) {
				throw new RuntimeException(String.format(
						"Missing mandatory parameter '%s' by %s", name,
						annotation.getClass().getName()));
			}
			return params.getDouble(name);
		} else if (type.equals(OptionalInt.class)) {
			return params.getOptionalInt(name);
		} else if (type.equals(OptionalLong.class)) {
			return params.getOptionalLong(name);
		} else if (type.equals(OptionalDouble.class)) {
			return params.getOptionalDouble(name);
		} else if (type.equals(Optional.class)) {
			// avans supports Optional<String> only.
			return params.getOptional(name);
		} else {
			throw new RuntimeException(String.format(
					"Unknown parameter type '%s' for '%s'", type, name));
		}
	}

	protected Optional<Object> GET_PARAMETER(final Parameter parameter) {
		return Optional.empty();
	}

	// You can hook this.
	protected WebResponse convertResponse(final Controller controller,
			final Object res) {
		if (res instanceof APIResponse) {
			// Rendering APIResponse with Jackson by default.
			return controller.renderJSON(res);
		} else {
			throw new RuntimeException(String.format(
					"Unknown return value from action: %s(%s)", Object.class,
					controller.getRequest().getPathInfo()));
		}
	}

	@Override
	public void close() {
	}

	@Override
	public Map<String, Object> getPluginStash() {
		return this.pluginStash;
	}

}
