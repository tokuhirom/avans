package me.geso.avans.session;

import java.util.Optional;

import me.geso.avans.Controller;
import me.geso.avans.trigger.ResponseFilter;
import me.geso.webscrew.response.WebResponse;

public interface SessionMixin extends Controller {
	static final String stashKey = "session";

	public default WebSessionManager getSession() {
		final Object session = this.computePluginStashValueIfAbsent(
				this.getClass(),
				stashKey, () -> {
					return this.buildSessionManager();
				});
		return (WebSessionManager) session;
	}

	public WebSessionManager buildSessionManager();

	@ResponseFilter
	public default void responseFilter(final WebResponse response) {
		final Optional<Object> maybeSession = this.getPluginStashValue(
				this.getClass(),
				stashKey);
		if (maybeSession.isPresent()) {
			final WebSessionManager session = (WebSessionManager) maybeSession
					.get();
			session.responseFilter(response);
		}
	}
}
