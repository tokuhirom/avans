package me.geso.testmech;

import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;

import lombok.Getter;
import lombok.SneakyThrows;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestMechResponse {

	@Getter
	private CloseableHttpResponse response;
	@Getter
	private byte[] content;

	public TestMechResponse(CloseableHttpResponse response,
			byte[] content) {
		this.response = response;
		this.content = content;
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

	public String getContentString() {
		return new String(content, Charset.forName("UTF-8"));
	}

	@SneakyThrows
	public <T> T readJSON(Class<T> valueType) {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(content, valueType);
	}

	public void assertSuccess() {
		int status = getStatus();
		Assert.assertTrue(200 <= status && status < 300);
	}

	public void assertStatusEquals(int statusCode) {
		int actual = getStatus();
		Assert.assertEquals(statusCode, actual);
	}

	public void assertContentTypeStartsWith(String prefix) {
		assertTrue(this.getContentType().startsWith(prefix));
	}

	public void assertContentTypeContains(String s) {
		assertTrue(this.getContentType().contains(s));
	}


	public void assertContentContains(String substring) {
		Assert.assertTrue(this.getContentString().contains(substring));
	}
}