package me.geso.avans.csp;

import java.security.SecureRandom;
import java.util.Base64;

import me.geso.avans.Controller;
import me.geso.avans.trigger.ResponseFilter;
import me.geso.webscrew.response.WebResponse;

public interface NoncePlugin extends Controller {
	static final int nonceLength = 64;

	public default String getNonce() {
		final String nonce = (String) this.computePluginStashIfAbsent(
				this.getClass(), "nonce", () -> {
					final SecureRandom random = new SecureRandom();
					final byte[] bytes = new byte[NoncePlugin.nonceLength];
					random.nextBytes(bytes);
					return Base64.getEncoder().encodeToString(bytes);
				});
		return nonce;
	}

	@ResponseFilter
	public default void injectResponse(final WebResponse response) {
		final String csp = "unsafe-inline; script-src 'nonce-"
				+ this.getNonce() + "'";
		response.addHeader("Content-Security-Policy", csp);
	}

}
