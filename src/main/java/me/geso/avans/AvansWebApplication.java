package me.geso.avans;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;

/**
 * You should create this object per HTTP request.
 * 
 * @author tokuhirom
 *
 */
public abstract class AvansWebApplication {
	private AvansRequest request;
	private HttpServletResponse servletResponse;

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

	public AvansResponse dispatch() {
		WebRouter<AvansAction> router = this.getRouter();
		String method = this.request.getMethod();
		String path = this.request.getPathInfo();
		RoutingResult<AvansAction> match = router.match(
				this.request.getMethod(), request.getPathInfo());
		if (!match.methodAllowed()) {
			return this.renderMethodNotAllowed();
		}

		Map<String, String> captured = match.getCaptured();
		AvansAction destination = match.getDestination();
		AvansResponse response = destination.run(this, captured);
		if (response == null) {
			throw new RuntimeException(String.format(
					"Response must not be null: %s, %s, %s",
					request.getMethod(), request.getPathInfo(),
					destination.toString()
					));
		}
		return response;
	}

	private AvansResponse renderMethodNotAllowed() {
		return this.renderError(405, "Method not allowed");
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

	public abstract WebRouter<AvansAction> getRouter();

	// hook point
	public Optional<AvansResponse> BEFORE_DISPATCH() {
		// override me.
		return Optional.empty();
	}

}
