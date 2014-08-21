package me.geso.servletmech;

import java.nio.charset.Charset;

import lombok.Getter;

import org.apache.http.client.methods.CloseableHttpResponse;

public class ServletMechResponse {

	@Getter
	private CloseableHttpResponse response;
	@Getter
	private byte[] body;

	public ServletMechResponse(CloseableHttpResponse response,
			byte[] body) {
		this.response = response;
		this.body = body;
	}

	public int getStatus() {
		return response.getStatusLine().getStatusCode();
	}
	
	public String getFirstHeader(String name) {
		return response.getFirstHeader(name).getValue();
	}

	public String getContentType() {
		return response.getFirstHeader("Content-Type").getValue();
	}

	public String getBodyString() {
		return new String(body, Charset.forName("UTF-8"));
	}
}