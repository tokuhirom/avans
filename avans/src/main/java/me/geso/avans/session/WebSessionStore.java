package me.geso.avans.session;

import java.util.Map;
import java.util.Optional;


public interface WebSessionStore {
	public void save(String sessionId, Map<String, Object> data);

	public Optional<Map<String, Object>> load(String sessionId);

	public void remove(String sessionId);
}
