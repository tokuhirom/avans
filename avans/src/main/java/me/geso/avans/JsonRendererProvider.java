package me.geso.avans;

import me.geso.webscrew.response.WebResponse;

public interface JsonRendererProvider {
	public WebResponse renderJSON(final Object obj);
}
