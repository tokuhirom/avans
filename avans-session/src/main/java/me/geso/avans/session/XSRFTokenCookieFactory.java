package me.geso.avans.session;

import javax.servlet.http.Cookie;

public interface XSRFTokenCookieFactory {
	public Cookie createCookie(final String data);

	/**
	 * Create new XSRF Token.
	 */
	public String createXSRFToken(final String src);
}
