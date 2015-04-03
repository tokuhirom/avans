package me.geso.avans.session;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;

import org.junit.Test;

public class DefaultXSRFTokenCookieFactoryTest {

	@SuppressWarnings("SpellCheckingInspection")
	@Test
	public void test() throws NoSuchAlgorithmException, InvalidKeyException {
		final SecretKeySpec signingKey = new SecretKeySpec(
				"My Secret".getBytes(), "HmacSHA1");

		final Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);

		final DefaultXSRFTokenCookieFactory factory = DefaultXSRFTokenCookieFactory
				.builder()
				.name("XSRFFF")
				.mac(mac)
				.build();

		// create cookie
		final Cookie cookie = factory.createCookie("fjkljoqwuaaaaaaaaiea");
		System.out.println(cookie.getValue());
		assertThat(cookie.getName(), is("XSRFFF"));

		// create xsrf token
		final String xsrf = factory.createXSRFToken("src");
		assertThat(xsrf, is("QaP5UPltP_cbjd24SenUiiYM5GU="));
	}

}
