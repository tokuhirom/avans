package me.geso.avans.session;

import javax.servlet.http.Cookie;

public interface XSRFTokenCookieFactory {
	public Cookie createCookie(final String data);
}
