package me.geso.avans.session;

import java.util.Optional;
import java.util.OptionalLong;

import me.geso.webscrew.response.WebResponse;

public interface WebSessionManager {

	/**
	 * This method may inject Cookie header to the session object.
	 * 
	 * @param response
	 */
	public void responseFilter(final WebResponse response);

	/**
	 * Get String value from current session.
	 * 
	 * @param key
	 * @return
	 */
	public Optional<String> getString(final String key);

	/**
	 * Get Long value from current Session.
	 * 
	 * @param key
	 * @return
	 */
	public OptionalLong getLong(final String key);

	/**
	 * Set {@code Long} value to the current session.
	 * 
	 * @param key
	 * @param value
	 */
	public void setLong(final String key, final long value);

	/**
	 * Set String value to current session.
	 * 
	 * @param key
	 * @param value
	 */
	void setString(final String key, final String value);

	/**
	 * Expire current session. Session manager impl will remove the data from
	 * storage.
	 */
	void expire();

	/**
	 * Change session ID.<br>
	 * This method is required for defending from session fixation attack.
	 */
	void changeSessionId();

	/**
	 * Remove data from the storage.
	 * 
	 * @param key
	 */
	public void remove(final String key);

}
