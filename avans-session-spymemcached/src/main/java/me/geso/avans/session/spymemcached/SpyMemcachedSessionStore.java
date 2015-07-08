package me.geso.avans.session.spymemcached;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import me.geso.avans.session.WebSessionStore;
import net.spy.memcached.MemcachedClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

public class SpyMemcachedSessionStore implements WebSessionStore {

	private final MemcachedClient memcachedClient;
	private final int expirationTime;
	private static final ObjectMapper defaultObjectMapper = new ObjectMapper(
			new CBORFactory());
	private final ObjectMapper objectMapper;

	/**
	 * Create new intance.
	 * 
	 * @param memcachedClient
	 *            Spymemcached instance.
	 * @param objectMapper
	 *            Jackson ObjectMapper instance. It's an
	 *            serializer/deserializer.
	 * @param expirationTime
	 *            Is a default expiration time for the session data.
	 *            <ul>
	 *            <li>If it's 0, the item never expires</li>
	 *            <li>If it's shorter than 30 days, the item will expires at
	 *            {@code expirationTime} seconds.</li>
	 *            <li>If you want longer than 30 days, it's fine, just use an
	 *            absolute time (time() + whatever).</li>
	 *            </ul>
	 */
	public SpyMemcachedSessionStore(
			@NonNull MemcachedClient memcachedClient,
			@NonNull ObjectMapper objectMapper,
			int expirationTime) {
		this.memcachedClient = memcachedClient;
		this.expirationTime = expirationTime;
		this.objectMapper = objectMapper;
	}

	/**
	 * Using CBOR serializer/deserializer.
	 * 
	 * @param memcachedClient
	 * @param expirationTime
	 */
	public SpyMemcachedSessionStore(
			MemcachedClient memcachedClient,
			int expirationTime) {
		this(memcachedClient, defaultObjectMapper, expirationTime);
	}

	@Override
	public Optional<Map<String, Object>> load(String sessionId) {
		final byte[] bytes = (byte[]) this.memcachedClient.get(sessionId);
		if (bytes == null) {
			return Optional.empty();
		}
		try {
			final Map<String, Object> readValue = objectMapper.readValue(bytes,
					new TypeReference<Map<String, Object>>() {
					});
			return Optional.of(readValue);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void save(String sessionId, Map<String, Object> data) {
		try {
			final byte[] entry = objectMapper.writeValueAsBytes(data);
			this.memcachedClient.set(sessionId, this.expirationTime,
					entry).get();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void remove(String sessionId) {
		this.memcachedClient.delete(sessionId);
	}

}
