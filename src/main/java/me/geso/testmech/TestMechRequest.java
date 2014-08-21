package me.geso.testmech;

import java.io.ByteArrayOutputStream;

import lombok.SneakyThrows;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class TestMechRequest {

	private HttpRequestBase request;
	private CookieStore cookieStore;

	public TestMechRequest(CookieStore cookieStore, HttpRequestBase request) {
		this.cookieStore = cookieStore;
		this.request = request;
	}

	public String getMethod() {
		return this.request.getMethod();
	}

	public TestMechRequest setHeader(String name, String value) {
		this.request.setHeader(name, value);
		return this;
	}
	
	public TestMechRequest addHeader(String name, String value) {
		this.request.addHeader(name, value);
		return this;
	}
	
	@SneakyThrows
	public TestMechResponse execute() {
		try (CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.build()) {
			try (CloseableHttpResponse response = httpclient
					.execute(request)) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				response.getEntity().writeTo(stream);
				byte[] byteArray = stream.toByteArray();
				return new TestMechResponse(response, byteArray);
			}
		}
	}
}
