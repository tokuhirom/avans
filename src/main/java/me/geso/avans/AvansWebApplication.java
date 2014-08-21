package me.geso.avans;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.SneakyThrows;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;

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
public abstract class AvansWebApplication implements Closeable {
	private AvansRequest request;
	private HttpServletResponse servletResponse;
	private Map<String, String> args;

	public AvansWebApplication(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws IOException {
		this.request = new AvansRequest(servletRequest);
		this.servletResponse = servletResponse;
	}

	public void run() throws IOException {
		{
			Optional<AvansResponse> maybeResponse = this.BEFORE_DISPATCH();
			if (maybeResponse.isPresent()) {
				maybeResponse.get().write(servletResponse);
			}
		}

		AvansResponse res = this.dispatch();
		if (res == null) {
			throw new RuntimeException("dispatch method must not return NULL");
		}
		res.write(servletResponse);
	}

	public AvansRequest getRequest() {
		return this.request;
	}

	public abstract AvansResponse dispatch();

	protected void setArgs(Map<String, String> captured) {
		this.args = captured;
	}
	
	public String getArg(String name) {
		String arg = this.args.get(name);
		if (arg == null) {
			throw new RuntimeException("Missing mandatory argument: " + name);
		}
		return arg;
	}

	public Optional<String> getOptionalArg(String name) {
		String arg = this.args.get(name);
		return Optional.ofNullable(arg);
	}

	public AvansResponse errorMethodNotAllowed() {
		return this.renderError(405, "Method not allowed");
	}

	public AvansResponse errorForbidden() {
		return this.renderError(403, "Forbidden");
	}

	private AvansResponse renderError(int code, String message) {
		AvansAPIResponse<String> apires = new AvansAPIResponse<>(code, message);

		AvansResponse res = this.renderJSON(apires);
		res.setStatus(code);
		return res;
	}

	@SneakyThrows
	public AvansResponse renderJSON(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);

		byte[] json = mapper.writeValueAsBytes(obj);
		AvansBytesResponse res = new AvansBytesResponse();
		res.setContentType("application/json; charset=utf-8");
		res.setContentLength(json.length);
		res.setBody(json);
		return res;
	}
	
	@SneakyThrows
	public AvansBytesResponse renderMustache(String template, Object context) {
		File tmplDir = this.getTemplateDirectory();
		DefaultMustacheFactory factory = new DefaultMustacheFactory(tmplDir);
		Mustache mustache = factory.compile(template);
		StringWriter writer = new StringWriter();
		mustache.execute(writer, context).flush();
		String bodyString = writer.toString();
		byte[] body = bodyString.getBytes(Charset.forName("UTF-8"));
		
		AvansBytesResponse res = new AvansBytesResponse();
		res.setContentType("text/html; charset=utf-8");
		res.setContentLength(body.length);
		res.setBody(body);
		return res;
	}

	public File getTemplateDirectory() {
		return new File(getBaseDirectory() + "/tmpl/");
	}
	
	public String getBaseDirectory() {
		return System.getProperty("user.dir");
	}

	// hook point
	public Optional<AvansResponse> BEFORE_DISPATCH() {
		// override me.
		return Optional.empty();
	}

}
