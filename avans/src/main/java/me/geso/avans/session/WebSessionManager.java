package me.geso.avans.session;

import java.util.Optional;
import java.util.OptionalLong;

import me.geso.webscrew.response.WebResponse;

public interface WebSessionManager {

	public void remove(String key);

	public void responseFilter(WebResponse response);

	public Optional<String> getString(String key);

	public OptionalLong getLong(String key);

	public void setLong(String key, long value);

	void setString(String key, String value);

	void expire();

	void regenerateSessionId();
}
