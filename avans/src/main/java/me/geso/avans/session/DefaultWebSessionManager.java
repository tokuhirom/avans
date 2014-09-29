package me.geso.avans.session;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import javax.crypto.Mac;
import javax.servlet.http.Cookie;

import lombok.NonNull;
import me.geso.webscrew.request.WebRequest;
import me.geso.webscrew.response.WebResponse;

public class DefaultWebSessionManager implements
		WebSessionManager {
	private final String sessionCookieName;
	private final WebRequest request;
	private final WebSessionStore sessionStore;
	private SessionData sessionData;
	private boolean sessionCookieHttpOnly;
	private boolean sessionCookieSecure;
	private int sessionCookieMaxAge;
	private boolean xsrfTokenCookieSecure;
	private final Mac xsrfTokenMac;
	private boolean expired;

	public DefaultWebSessionManager(@NonNull final String sessionCookieName,
			@NonNull final WebRequest request,
			@NonNull final WebSessionStore sessionStore,
			@NonNull final Mac xsrfTokenMac) {
		this.sessionCookieName = sessionCookieName;
		this.request = request;
		this.sessionStore = sessionStore;
		this.sessionCookieMaxAge = 24 * 60 * 60;
		this.sessionCookieHttpOnly = true;
		this.sessionCookieSecure = false;
		this.xsrfTokenCookieSecure = false;
		this.xsrfTokenMac = xsrfTokenMac;
		this.expired = false;
	}

	private SessionData getSessionData() {
		if (this.expired) {
			throw new IllegalStateException(
					"You can't call this method for expired session.");
		}

		if (this.sessionData == null) {
			final Optional<SessionData> loadSession = this.loadSession();
			if (loadSession.isPresent()) {
				return loadSession.get();
			}

			final String sessionId = this.generateSessionId();
			this.sessionData = SessionData.startNewSession(sessionId);
			return this.sessionData;
		} else {
			return this.sessionData;
		}
	}

	private Optional<SessionData> loadSession() {
		final Cookie[] cookies = this.request.getCookies();
		if (cookies != null) {
			for (final Cookie cookie : cookies) {
				if (this.sessionCookieName.equals(cookie.getName())) {
					final String sessionId = cookie.getValue();
					final Optional<Map<String, Object>> data = this.sessionStore
							.load(sessionId);
					if (data.isPresent()) {
						this.sessionData = new SessionData(sessionId,
								data.get(), false);
						return Optional.of(this.sessionData);
					}
				}
			}
		}
		return Optional.empty();
	}

	private String generateSessionId() {
		// create session
		SecureRandom secureRandom;
		try {
			secureRandom = SecureRandom
					.getInstanceStrong();
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		final byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		final String sessionId = Base64.getEncoder().encodeToString(
				bytes);
		return sessionId;
	}

	@Override
	public void setString(final String key, final String value) {
		this.getSessionData().setString(key, value);
	}

	@Override
	public void setLong(final String key, final long value) {
		this.getSessionData().setLong(key, value);
	}

	@Override
	public Optional<String> getString(final String key) {
		return this.getSessionData().getString(key);
	}

	@Override
	public OptionalLong getLong(final String key) {
		return this.getSessionData().getLong(key);
	}

	@Override
	public void remove(final String key) {
		this.getSessionData().remove(key);
	}

	private static class SessionData {
		private final String sessionId;
		private final Map<String, Object> data;
		private final boolean isFresh;
		private boolean isDirty;

		SessionData(final String sessionId,
				@NonNull final Map<String, Object> data,
				final boolean isFresh) {
			this.sessionId = sessionId;
			this.data = data;
			this.isFresh = isFresh;
		}

		public static SessionData startNewSession(final String sessionId) {
			return new SessionData(
					sessionId,
					new HashMap<>(),
					true);
		}

		public String getSessionId() {
			return this.sessionId;
		}

		public Optional<String> getString(final String key) {
			if (this.data.containsKey(key)) {
				final Object value = this.data.get(key);
				if (value instanceof String) {
					return Optional.of((String) value);
				} else {
					throw new RuntimeException();
				}
			} else {
				return Optional.empty();
			}
		}

		public OptionalLong getLong(final String key) {
			if (this.getData().containsKey(key)) {
				final Object value = this.data.get(key);
				if (value instanceof Long) {
					return OptionalLong.of((Long) value);
				} else if (value instanceof Integer) {
					final Integer i = (Integer) value;
					return OptionalLong.of(i.longValue());
				} else {
					throw new RuntimeException(String.format(
							"value for '%s' should be long. But: %s", key,
							value.getClass()));
				}
			} else {
				return OptionalLong.empty();
			}
		}

		public void setLong(final String key, final long value) {
			this.isDirty = true;
			this.getData().put(key, value);
		}

		public void setString(final String key, final String value) {
			this.isDirty = true;
			this.getData().put(key, value);
		}

		public void remove(final String key) {
			this.getData().remove(key);
			this.isDirty = true;
		}

		public boolean isFresh() {
			return this.isFresh;
		}

		public boolean isDirty() {
			return this.isDirty;
		}

		public Map<String, Object> getData() {
			return this.data;
		}

		public void setDirty() {
			this.isDirty = true;
		}

	}

	@Override
	public void responseFilter(final WebResponse response) {
		if (this.sessionData != null
				&& (this.sessionData.isDirty() || this.sessionData
						.isFresh())) {
			this.sessionStore.save(this.sessionData.getSessionId(),
					this.sessionData.getData());
			response.addCookie(this.buildSessionCookie());
			response.addCookie(this.buildXsrfTokenCookie());
		}
	}

	public Cookie buildSessionCookie() {
		final Cookie cookie = new Cookie(
				this.sessionCookieName, this.sessionData.sessionId
				);
		cookie.setHttpOnly(this.isSessionCookieHttpOnly());
		cookie.setSecure(this.isSessionCookieSecure());
		cookie.setMaxAge(this.getSessionCookieMaxAge());
		return cookie;
	}

	public Cookie buildXsrfTokenCookie() {
		final byte[] token = this.getXsrfTokenMac()
				.doFinal(this.sessionData.sessionId.getBytes());
		final Cookie cookie = new Cookie(
				"XSRF-TOKEN", Base64.getEncoder().encodeToString(token)
				);
		// I want to read this cookie from java script.
		cookie.setHttpOnly(false);
		cookie.setSecure(this.isXsrfTokenCookieSecure());
		// It should be same as session token's max-age.
		cookie.setMaxAge(this.getSessionCookieMaxAge());
		return cookie;
	}

	protected boolean getSessionCookieHttpOnly() {
		return this.sessionCookieHttpOnly;
	}

	public boolean isSessionCookieHttpOnly() {
		return this.sessionCookieHttpOnly;
	}

	public void setSessionCookieHttpOnly(final boolean sessionCookieHttpOnly) {
		this.sessionCookieHttpOnly = sessionCookieHttpOnly;
	}

	public boolean isSessionCookieSecure() {
		return this.sessionCookieSecure;
	}

	public void setSessionCookieSecure(final boolean sessionCookieSecure) {
		this.sessionCookieSecure = sessionCookieSecure;
	}

	public int getSessionCookieMaxAge() {
		return this.sessionCookieMaxAge;
	}

	public boolean isXsrfTokenCookieSecure() {
		return this.xsrfTokenCookieSecure;
	}

	public void setXsrfTokenCookieSecure(final boolean xsrfTokenCookieSecure) {
		this.xsrfTokenCookieSecure = xsrfTokenCookieSecure;
	}

	public void setSessionCookieMaxAge(final int sessionCookieMaxAge) {
		this.sessionCookieMaxAge = sessionCookieMaxAge;
	}

	public Mac getXsrfTokenMac() {
		return this.xsrfTokenMac;
	}

	@Override
	public void expire() {
		// remove current session data from storage.
		this.sessionStore.remove(this.getSessionData().getSessionId());
		this.expired = true;
	}

	@Override
	public void changeSessionId() {
		final Optional<SessionData> currentSession = this.loadSession();
		Map<String, Object> data;
		if (currentSession.isPresent()) {
			data = currentSession.get().getData();
			final String sessionId = currentSession.get().getSessionId();
			// remove current sessionid from storage.
			this.sessionStore.remove(sessionId);
		} else {
			data = new HashMap<>();
		}

		final String newSessionId = this.generateSessionId();
		this.sessionData = new SessionData(newSessionId,
				data, true);
		this.sessionData.setDirty();
	}

}
