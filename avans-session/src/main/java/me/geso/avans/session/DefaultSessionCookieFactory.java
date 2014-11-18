package me.geso.avans.session;

import javax.servlet.http.Cookie;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DefaultSessionCookieFactory implements SessionCookieFactory {
	/**
	 * Cookie's name
	 */
	@NonNull
	private final String name;

	@NonNull
	private final String path;
	private final boolean httpOnly;
	private final boolean secure;
	private final int maxAge;

	@Override
	public Cookie createCookie(final String sessionId) {
		final Cookie cookie = new Cookie(this.name, sessionId);
		cookie.setPath(this.path);
		cookie.setHttpOnly(this.httpOnly);
		cookie.setSecure(this.secure);
		cookie.setMaxAge(this.maxAge);
		return cookie;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public static DefaultSessionCookieFactoryBuilder builder() {
		return new DefaultSessionCookieFactoryBuilder();
	}

	@Accessors(fluent = true)
	@Setter
	public static class DefaultSessionCookieFactoryBuilder {
		@NonNull
		private String name = "avans_session_id";
		@NonNull
		private String path = "/";
		private boolean httpOnly = true;
		private boolean secure = false;
		private int maxAge = 24 * 60 * 60;

		public DefaultSessionCookieFactory build() {
			return new DefaultSessionCookieFactory(this.name, this.path,
					this.httpOnly, this.secure, this.maxAge);
		}
	}
}
