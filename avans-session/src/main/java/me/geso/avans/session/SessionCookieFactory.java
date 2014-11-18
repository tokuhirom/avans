package me.geso.avans.session;

import javax.servlet.http.Cookie;

public interface SessionCookieFactory {
	public String getName();
	public Cookie createCookie(final String sessionId);
}
