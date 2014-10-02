package me.geso.avans;

import me.geso.webscrew.response.WebResponse;

public interface ErrorPageRenderer {

	/**
	 * Render the error response.
	 *
	 * @param code
	 * @param message
	 * @return
	 */
	public WebResponse renderError(final int code,
			final String message);
}
