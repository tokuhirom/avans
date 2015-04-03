package me.geso.avans.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.ToString;

/**
 * In memory hash session storage for testing/debugging.
 * Do not use in production environment.
 * 
 * @author tokuhirom
 *
 */
@ToString
public class HashMapSessionStore implements WebSessionStore {
	private final Map<String, Map<String, Object>> storage = new HashMap<>();

	@Override
	public void save(final String sessionId, final Map<String, Object> data) {
		this.storage.put(sessionId, data);
	}

	@Override
	public Optional<Map<String, Object>> load(final String sessionId) {
		final Map<String, Object> map = this.storage.get(sessionId);
		if (map == null) {
			return Optional.empty();
		} else {
			return Optional.of(map);
		}
	}

	@Override
	public void remove(final String sessionId) {
		this.storage.remove(sessionId);
	}

}
