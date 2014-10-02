package me.geso.avans;

import lombok.NonNull;
import me.geso.webscrew.response.WebResponse;

public interface JSONErrorPageRenderer extends ErrorPageRenderer,
		JSONRendererProvider {

	/**
	 * Render the error response.
	 *
	 * @param code
	 * @param message
	 * @return
	 */
	@Override
	public default WebResponse renderError(final int code,
			@NonNull final String message) {
		final APIResponse<String> apires = new APIResponse<>(code, message,
				null);

		final WebResponse res = this.renderJSON(code, apires);
		return res;
	}
}
