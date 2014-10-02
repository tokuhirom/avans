package me.geso.avans;

import me.geso.webscrew.response.WebResponse;

public interface JSONRendererProvider {

	/**
	 * Rendering JSON response.
	 * 
	 * @param statusCode
	 * @param obj
	 * @return
	 */
	public WebResponse renderJSON(final int statusCode, final Object obj);
}
