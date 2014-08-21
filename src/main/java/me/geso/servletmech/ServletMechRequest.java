package me.geso.servletmech;

import java.io.ByteArrayOutputStream;

import lombok.SneakyThrows;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ServletMechRequest {

	private HttpRequestBase request;
	private CookieStore cookieStore;

	public ServletMechRequest(CookieStore cookieStore, HttpRequestBase request) {
		this.cookieStore = cookieStore;
		this.request = request;
	}

	public String getMethod() {
		return this.request.getMethod();
	}

	public ServletMechRequest setHeader(String name, String value) {
		this.request.setHeader(name, value);
		return this;
	}
	
	
	public ServletMechRequest addHeader(String name, String value) {
		this.request.addHeader(name, value);
		return this;
	}
	
	@SneakyThrows
	public ServletMechResponse execute() {
		try (CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.build()) {
			try (CloseableHttpResponse response = httpclient
					.execute(request)) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				response.getEntity().writeTo(stream);
				byte[] byteArray = stream.toByteArray();
				return new ServletMechResponse(response, byteArray);
			}
		}
	}
}
