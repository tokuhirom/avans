package me.geso.avans.session;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Session ID generator uses SecureRandom.<br>
 * This class encodes bytes by url safe Base64 encoder.<br>
 *
 */
public class SecureRandomSessionIDGenerator implements SessionIDGenerator {
	private final SecureRandom secureRandom;
	private final int length;

	/**
	 * Create instance.
	 * 
	 * @param secureRandom
	 *            SecureRandom object.
	 * @param length
	 *            Required session id length.
	 */
	public SecureRandomSessionIDGenerator(final SecureRandom secureRandom,
			final int length) {
		this.secureRandom = secureRandom;
		this.length = length;
	}

	@Override
	public String generate() {
		final byte[] bytes = new byte[this.length];
		this.secureRandom.nextBytes(bytes);
		final String sessionId = Base64.getUrlEncoder().encodeToString(
				bytes);
		return sessionId.substring(0, this.length);
	}
}
