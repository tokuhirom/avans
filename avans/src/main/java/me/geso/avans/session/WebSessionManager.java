package me.geso.avans.session;

import java.util.Optional;
import java.util.OptionalLong;

import me.geso.webscrew.response.WebResponse;

public interface WebSessionManager {

	public void remove(final String key);

	public void responseFilter(final WebResponse response);

	public Optional<String> getString(final String key);

	public OptionalLong getLong(final String key);

	public void setLong(final String key, final long value);

	void setString(final String key, final String value);

	void expire();

	void regenerateSessionId();
}
