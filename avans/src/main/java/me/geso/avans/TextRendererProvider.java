package me.geso.avans;

import me.geso.webscrew.response.WebResponse;

public interface TextRendererProvider {

	/**
	 * Create new text/plain response.
	 *
	 * @param text
	 * @return
	 */
	public WebResponse renderText(final String text);
}
