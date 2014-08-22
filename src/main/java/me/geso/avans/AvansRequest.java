package me.geso.avans;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import lombok.SneakyThrows;

import com.fasterxml.jackson.databind.ObjectMapper;

// TODO port all methods from Plack::Request
public class AvansRequest {
	private HttpServletRequest request;

	public AvansRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getPathInfo() {
		return this.request.getPathInfo();
	}

	public String getHeader(String name) {
		return this.request.getHeader(name);
	}

	public List<String> getHeaders(String name) {
		return Collections.list(this.request.getHeaders(name));
	}

	public int getContentLength() {
		return this.request.getContentLength();
	}

	public String getMethod() {
		return this.request.getMethod();
	}

	public String getRequestURI() {
		return this.request.getRequestURI();
	}

	public String getQueryString() {
		return this.request.getQueryString();
	}

	public Cookie[] getCookies() {
		return this.request.getCookies();
	}

	public HttpSession getSession() {
		return this.request.getSession();
	}
	
	public void changeSessionId() {
		this.request.changeSessionId();
	}

	/**
	 * Read JSON from content-body. And parse it. This method runs hibernate
	 * validator. If the validation was failed, it throws runtime exception.
	 * 
	 * @param klass
	 * @return
	 */
	@SneakyThrows
	public <T> T readJSON(Class<T> klass) {
		ServletInputStream inputStream = this.request.getInputStream();

		ObjectMapper mapper = new ObjectMapper();
		T instance = mapper.readValue(inputStream, klass);
		if (instance != null) {
			this.validate(instance);
			return instance;
		} else {
			throw new RuntimeException("null found... in content body");
		}
	}

	public <T> void validate(T o) {
		ValidatorFactory validatorFactory = Validation
				.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<T>> violations = validator.validate(o);
		if (!violations.isEmpty()) {
			String message = violations.stream().map(e -> {
				return e.getPropertyPath() + " " + e.getMessage();
			}).collect(Collectors.joining(","));
			throw new AvansValidationException(message);
		}
	}

	/**
	 * Read parameters from query string/content-body
	 * 
	 * @param name
	 * @return
	 */
	public String getParameter(String name) {
		return this.request.getParameter(name);
	}

	public Map<String, String[]> getParameterMap(String name) {
		return this.request.getParameterMap();
	}

	public OptionalInt getIntParam(String name) {
		String parameter = this.request.getParameter(name);
		if (parameter != null) {
			return OptionalInt.of(Integer.parseInt(parameter));
		} else {
			return OptionalInt.empty();
		}
	}

	// TODO: support ServletFileUpload

}
