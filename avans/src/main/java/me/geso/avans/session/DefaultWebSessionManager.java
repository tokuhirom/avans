package me.geso.avans.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import javax.servlet.http.Cookie;

import lombok.NonNull;
import me.geso.webscrew.request.WebRequest;
import me.geso.webscrew.response.WebResponse;

public class DefaultWebSessionManager implements
		WebSessionManager {
	@NonNull
	private final WebRequest request;
	@NonNull
	private final WebSessionStore sessionStore;
	@NonNull
	private final SessionCookieFactory sessionCookieFactory;
	@NonNull
	private final SessionIDGenerator sessionIDGenerator;
	@NonNull
	private final XSRFTokenCookieFactory xsrfTokenCookieFactory;

	public DefaultWebSessionManager(WebRequest request,
			WebSessionStore sessionStore,
			SessionIDGenerator sessionIDGenerator,
			SessionCookieFactory sessionCookieFactory,
			XSRFTokenCookieFactory xsrfTokenCookieFactory
			) {
		this.request = request;
		this.sessionStore = sessionStore;
		this.sessionIDGenerator = sessionIDGenerator;
		this.sessionCookieFactory = sessionCookieFactory;
		this.xsrfTokenCookieFactory = xsrfTokenCookieFactory;
	}

	private SessionData sessionData;

	private boolean expired = false;

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
				if (this.sessionCookieFactory.getName()
						.equals(cookie.getName())) {
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
		return this.sessionIDGenerator.generate();
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

			final Cookie sessionCookie = this.sessionCookieFactory
					.createCookie(this.sessionData.sessionId);
			response.addCookie(sessionCookie);

			final Cookie xsrfTokenCookie = this.xsrfTokenCookieFactory
					.createCookie(this.sessionData.sessionId);
			response.addCookie(xsrfTokenCookie);
		}
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
