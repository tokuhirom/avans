package me.geso.avans.session;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.servlet.http.Cookie;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DefaultXSRFTokenCookieFactory implements XSRFTokenCookieFactory {

	private final String name;

	private final String path;

	private final boolean secure;

	private final boolean httpOnly;

	@NonNull
	private final Mac mac;

	private final int maxAge;

	@Override
	public Cookie createCookie(final String src) {
		final String encoded = createXSRFToken(src);
		final Cookie cookie = new Cookie(
				this.name, encoded
				);
		cookie.setPath(this.path);
		cookie.setHttpOnly(this.httpOnly);
		cookie.setSecure(this.secure);
		cookie.setMaxAge(this.maxAge);
		return cookie;
	}

	@Override
	public String createXSRFToken(final String src) {
		byte[] token;
		try {
			token = this.mac.doFinal(src.getBytes(StandardCharsets.UTF_8));
		} catch (final IllegalStateException e) {
			// ARIENAI.
			throw new RuntimeException(e);
		}

		return Base64.getUrlEncoder().encodeToString(token);
	}

	public static DefaultXSRFTokenCookieFactoryBuilder builder() {
		return new DefaultXSRFTokenCookieFactoryBuilder();
	}

	@Accessors(fluent = true)
	@Setter
	public static class DefaultXSRFTokenCookieFactoryBuilder {

		private String name = "XSRF-TOKEN";

		private String path = "/";

		private boolean secure = false;

		/**
		 * false by default. Because javascript framework(e.g. angularjs) needs
		 * to read this.
		 */
		private boolean httpOnly = false;

		@NonNull
		private Mac mac;

		/**
		 * This value should be same as session token's max-age value.
		 */
		private int maxAge = 24 * 60 * 60;

		DefaultXSRFTokenCookieFactoryBuilder() {
		}

		public DefaultXSRFTokenCookieFactory build() {
			return new DefaultXSRFTokenCookieFactory(this.name, this.path,
					this.secure, this.httpOnly, this.mac, this.maxAge);
		}
	}

}
