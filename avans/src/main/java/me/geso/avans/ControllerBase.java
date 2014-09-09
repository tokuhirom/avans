package me.geso.avans;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.methodparameter.DefaultMethodParameterBuilder;
import me.geso.avans.methodparameter.MethodParameterBuilder;
import me.geso.avans.methodparameter.Param;
import me.geso.tinyvalidator.ConstraintViolation;
import me.geso.tinyvalidator.Validator;
import me.geso.webscrew.Parameters;
import me.geso.webscrew.request.WebRequest;
import me.geso.webscrew.request.impl.DefaultWebRequest;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.RedirectResponse;
import me.geso.webscrew.response.WebResponse;

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
		this.request = this.createWebReqeust(servletRequest);
		this.servletResponse = servletResponse;
		this.setDefaultCharacterEncoding();

		MultiMap<String, String> pathParameters = new MultiValueMap<String, String>();
		for (String key : captured.keySet()) {
			pathParameters.put(key, captured.get(key));
		}
		this.pathParameters = new Parameters(pathParameters);
		this.AFTER_INIT();
	}

	public WebRequest createWebReqeust(HttpServletRequest servletRequest) {
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

	public WebRequest getRequest() {
		return this.request;
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
		APIResponse<String> apires = new APIResponse<>(code, message, null);

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
	public WebResponse renderText(String text) {
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
		return this.getBaseDirectory().resolve("templates");
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
	public Optional<WebResponse> BEFORE_DISPATCH() {
		// override me.
		return Optional.empty();
	}

	/**
	 * This is a hook point. You can override this method.
	 * 
	 * @param res
	 */
	public void AFTER_DISPATCH(WebResponse res) {
		return; // NOP
	}

	@SneakyThrows
	public void invoke(Method method, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse, Map<String, String> captured) {
		this.init(servletRequest, servletResponse, captured);

		WebResponse response = this.makeResponse(this, method);
		this.AFTER_DISPATCH(response);
		response.write(servletResponse);
	}

	@SneakyThrows
	private WebResponse makeResponse(Controller controller, Method method) {
		{
			Optional<WebResponse> maybeResponse = controller.BEFORE_DISPATCH();
			if (maybeResponse.isPresent()) {
				return maybeResponse.get();
			}
		}

		MethodParameterBuilder builder = this.createMethodParameterBuilder();
		Param[] params = builder.build(this, method);
		{
			Optional<WebResponse> webResponse = this.validateParameters(params);
			if (webResponse.isPresent()) {
				return webResponse.get();
			}
		}
		Object[] objectParams = Arrays.stream(params).map(it -> it.getObject())
				.toArray();
		Object res = method.invoke(controller, objectParams);
		if (res instanceof WebResponse) {
			return (WebResponse) res;
		} else if (res == null) {
			throw new RuntimeException(
					"dispatch method must not return NULL");
		} else {
			return this.convertResponse(controller, res);
		}
	}

	private Optional<WebResponse> validateParameters(Param[] params) {
		Validator validator = new Validator();
		List<String> messages = new ArrayList<>();
		for (Param param : params) {
			Object object = param.getObject();
			Annotation[] annotations = param.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonParam) {
					List<ConstraintViolation<Object>> validate = validator
							.validate(object);
					validate.stream().forEach(
							violation -> {
								String message = violation.getPropertyPath() + " "
										+ violation.getMessage();
								messages.add(message);
							});
				}
			}
		}
		// TODO validate parameters.
		if (messages.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(this.renderJSON(new APIResponse<>(403,
					messages, null)));
		}
	}

	public MethodParameterBuilder createMethodParameterBuilder() {
		return new DefaultMethodParameterBuilder();
	}

	// You can hook this.
	protected WebResponse convertResponse(Controller controller, Object res) {
		if (res instanceof APIResponse) {
			// Rendering APIResponse with Jackson by default.
			return controller.renderJSON(res);
		} else {
			throw new RuntimeException(String.format(
					"Unknown return value from action: %s(%s)", Object.class,
					controller.getRequest().getPathInfo()));
		}
	}
	
	public void close() {
	}

}
