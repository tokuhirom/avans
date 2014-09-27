package me.geso.avans.session;

import me.geso.avans.Controller;
import me.geso.avans.trigger.ResponseFilter;
import me.geso.webscrew.response.WebResponse;

public interface SessionMixin extends Controller {
	static final String stashKey = "Session:session";

	public default WebSessionManager getSession() {
		Object session = this.getPluginStash().get(SessionMixin.stashKey);
		if (session == null) {
			session = this.buildSessionManager();
			this.getPluginStash().put(SessionMixin.stashKey, session);
		}
		return (WebSessionManager) session;
	}

	public WebSessionManager buildSessionManager();

	@ResponseFilter
	public default void responseFilter(final WebResponse response) {
		final Object session = this.getPluginStash().get(SessionMixin.stashKey);
		if (session != null) {
			if (session instanceof WebSessionManager) {
				((WebSessionManager) session).responseFilter(response);
			}
		}
	}
}
