package me.geso.avans.session;

import java.util.Optional;
import java.util.OptionalLong;

import me.geso.webscrew.response.WebResponse;

public interface WebSessionManager {

	/**
	 * Get sesion ID
	 */
	public String getSessionId();

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

	public String getXSRFToken();

	/**
	 * Set String value to current session.
	 * 
	 * @param key
	 * @param value
	 */
	public void setString(final String key, final String value);

	/**
	 * Expire current session. Session manager impl will remove the data from
	 * storage.
	 */
	public void expire();

	/**
	 * Change session ID.<br>
	 * This method is required for defending from session fixation attack.
	 */
	public void changeSessionId();

	/**
	 * Remove data from the storage.
	 * 
	 * @param key
	 */
	public void remove(final String key);

	public boolean validateXSRFToken(String xsrfToken);
}
