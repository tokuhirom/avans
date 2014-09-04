package me.geso.avans;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.QueryParam;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

/**
 * You should create this object per HTTP request.
 * 
 * @author tokuhirom
 *
 */
public abstract class ControllerBase implements Controller {
	private WebRequest request;
	private HttpServletResponse servletResponse;
	private Parameters pathParameters;
	private static final Charset UTF8 = Charset.forName("UTF-8");

	public void init(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse,
			Map<String, String> captured) {
		this.BEFORE_INIT();
		this.request = new WebRequest(servletRequest);
		this.servletResponse = servletResponse;

		MultiMap<String, String> pathParameters = new MultiValueMap<String, String>();
		for (String key: captured.keySet()) {
			pathParameters.put(key, captured.get(key));
		}
		this.pathParameters = new Parameters(pathParameters);
		this.AFTER_INIT();
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
	}

	private WebResponse makeResponse(Method method) {
		{
			Optional<WebResponse> maybeResponse = this.BEFORE_DISPATCH();
			if (maybeResponse.isPresent()) {
				return maybeResponse.get();
			}
		}

		try {
			Object[] params = this.makeParameters(method);
			WebResponse res = (WebResponse) method.invoke(this, params);
			if (res == null) {
				throw new RuntimeException(
						"dispatch method must not return NULL");
			}
			return res;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public Object[] makeParameters(Method method) {
		Parameter[] parameters = method.getParameters();
		Class<?>[] types = method.getParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Object[] params = new Object[parameters.length];
		for (int i = 0; i < parameters.length; ++i) {
			for (Annotation annotation : parameterAnnotations[i]) {
				if (annotation instanceof JsonParam) {
					Object param = this.getRequest().readJSON(types[i]);
					params[i] = param;
				} else if (annotation instanceof QueryParam) {
					String name = ((QueryParam) annotation).value();
					Class<?> type = types[i];
					params[i] = getParameter(name, type, this.getRequest()
							.getQueryParams());
				} else if (annotation instanceof BodyParam) {
					String name = ((BodyParam) annotation).value();
					Class<?> type = types[i];
					params[i] = getParameter(name, type, this.getRequest()
							.getBodyParams());
				}
			}
		}
		return params;
	}

	private Object getParameter(String name, Class<?> type,
			Parameters params) {
		if (type.equals(int.class)) {
			return params.getInt(name);
		} else if (type.equals(long.class)) {
			return params.getLong(name);
		} else if (type.equals(String.class)) {
			return params.get(name);
		} else {
			throw new RuntimeException(String.format(
					"Unknown parameter type '%s' for '%s'", type, name));
		}
	}

	public WebRequest getRequest() {
		return this.request;
	}

	public void dispatch(Method method) {
		try {
			this.setDefaultCharacterEncoding();

			WebResponse res = this.makeResponse(method);
			this.AFTER_DISPATCH(res);
			res.write(servletResponse);
		} catch (IllegalArgumentException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get a path parameter.
	 * 
	 * @return
	 */
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

	public WebResponse errorForbidden(String message) {
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
	protected WebResponse renderError(int code, @NonNull String message) {
		APIResponse<String> apires = new APIResponse<>(code, message);

		ByteArrayResponse res = this.renderJSON(apires);
		res.setStatus(code);
		return res;
	}

	/**
	 * Create new redirect response. You can use relative url here.
	 * 
	 * @param location
	 * @return
	 */
	public RedirectResponse redirect(@NonNull String location) {
		return new RedirectResponse(location);
	}

	/**
	 * Create new text/plain response.
	 * 
	 * @param text
	 * @return
	 */
	@SneakyThrows
	public WebResponse renderTEXT(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text must not be null");
		}
		byte[] bytes = text.getBytes(UTF8);

		ByteArrayResponse res = new ByteArrayResponse();
		res.setContentType("text/plain; charset=utf-8");
		res.setBody(bytes);
		return res;
	}

	/**
	 * Rendering JSON by jackson.
	 * 
	 * @param obj
	 * @return
	 */
	@SneakyThrows
	public ByteArrayResponse renderJSON(Object obj) {
		ObjectMapper mapper = createObjectMapper();
		byte[] json = mapper.writeValueAsBytes(obj);

		ByteArrayResponse res = new ByteArrayResponse();
		res.setContentType("application/json; charset=utf-8");
		res.setContentLength(json.length);
		res.setBody(json);
		return res;
	}

	/**
	 * Create new ObjectMapper instance. You can override this method for
	 * customizing.
	 * 
	 * @return
	 */
	protected ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
		return mapper;
	}

	/**
	 * Create a response object by mustache template engine.
	 * 
	 * @param template
	 * @param context
	 * @return
	 */
	@SneakyThrows
	public ByteArrayResponse renderMustache(@NonNull String template,
			Object context) {
		Path tmplDir = this.getTemplateDirectory();
		DefaultMustacheFactory factory = new DefaultMustacheFactory(
				tmplDir.toFile());
		Mustache mustache = factory.compile(template);
		StringWriter writer = new StringWriter();
		mustache.execute(writer, context).flush();
		String bodyString = writer.toString();
		byte[] body = bodyString.getBytes(Charset.forName("UTF-8"));

		ByteArrayResponse res = new ByteArrayResponse();
		res.setContentType("text/html; charset=utf-8");
		res.setContentLength(body.length);
		res.setBody(body);
		return res;
	}

	public Path getTemplateDirectory() {
		return this.getBaseDirectory().resolve("tmpl");
	}

	/**
	 * Get project base directory. TODO: better jar location detection
	 * algorithm.
	 * 
	 * @return
	 */
	@SneakyThrows
	public Path getBaseDirectory() {
		return AvansUtil.getBaseDirectory(this.getClass());
	}

	/**
	 * This is a hook point. You can override this.
	 * 
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

	/**
	 * This is a hook point. You can override this method.
	 * 
	 * @param res
	 */
	protected void AFTER_DISPATCH(WebResponse res) {
		return; // NOP
	}

}
