package me.geso.avans.session;

import java.util.Map;
import java.util.Optional;

public interface WebSessionStore {
	public void save(final String sessionId, final Map<String, Object> data);

	public Optional<Map<String, Object>> load(final String sessionId);

	public void remove(final String sessionId);
}
