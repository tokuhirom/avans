package me.geso.avans;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

	/**
	 * Read JSON from content-body. And parse it.
	 * 
	 * @param klass
	 * @return
	 */
	@SneakyThrows
	public <T> T readJSON(Class<T> klass) {
		ServletInputStream inputStream = this.request.getInputStream();

		ObjectMapper mapper = new ObjectMapper();
		T instance = mapper.readValue(inputStream, klass);
		return instance;
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

	// TODO: support ServletFileUpload

}
