package me.geso.testmech;

import java.net.URI;

import lombok.SneakyThrows;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerator;

public class TestMech {
	private BasicCookieStore cookieStore = new BasicCookieStore();
	private String baseURL;

	public TestMech() { }
	
	public TestMech(String baseURL) {
		this.baseURL = baseURL;
	}
	
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public void addCookie(Cookie cookie) {
		this.cookieStore.addCookie(cookie);
	}

	@SneakyThrows
	public TestMechRequest get(String path) {
		URI url = new URIBuilder(baseURL).setPath(path).build();
		HttpGet get = new HttpGet(url);
		return new TestMechRequest(cookieStore, get);
	}

	@SneakyThrows
	public <T> TestMechRequest postJSON(String path, T params) {
		if (params == null) {
			throw new RuntimeException("Params should not be null");
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
		byte[] json = mapper.writeValueAsBytes(params);
		URI url = new URIBuilder(baseURL).setPath(path).build();
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type",
				"application/json; charset=utf-8");
		post.setEntity(new ByteArrayEntity(json));
		return new TestMechRequest(cookieStore, post);
	}
}
