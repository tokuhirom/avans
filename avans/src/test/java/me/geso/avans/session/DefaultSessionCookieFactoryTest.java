package me.geso.avans.session;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.servlet.http.Cookie;

import org.junit.Test;

public class DefaultSessionCookieFactoryTest {

	@Test
	public void test() {
		final DefaultSessionCookieFactory factory = DefaultSessionCookieFactory
				.builder()
				.name("avans_session_id3")
				.build();
		assertThat(factory.getName(), is("avans_session_id3"));
		final Cookie cookie = factory.createCookie("hogehoghoge");
		assertThat(cookie.getName(), is("avans_session_id3"));
		assertThat(cookie.getValue(), is("hogehoghoge"));
	}

}
