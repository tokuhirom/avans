package me.geso.avans.session;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;

import org.junit.Test;

public class DefaultXSRFTokenCookieFactoryTest {

	@Test
	public void test() throws NoSuchAlgorithmException, InvalidKeyException {
		final KeyGenerator kg = KeyGenerator.getInstance("HmacSHA1");
		final SecretKey sk = kg.generateKey();

		final Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(sk);

		final DefaultXSRFTokenCookieFactory factory = DefaultXSRFTokenCookieFactory
				.builder()
				.name("XSRFFF")
				.mac(mac)
				.build();
		final Cookie cookie = factory.createCookie("fjkljoqwuaaaaaaaaiea");
		System.out.println(cookie.getValue());
		assertThat(cookie.getName(), is("XSRFFF"));
	}

}
