package me.geso.avans;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;
import lombok.ToString;

/**
 * The response object using callback.
 * This class is useful for streaming response like CSV.
 */
@ToString
public class AvansCallbackResponse implements AvansResponse {

	private Callback callback;

	public AvansCallbackResponse(@NonNull Callback callback) {
		this.callback = callback;
	}

	@Override
	public void write(HttpServletResponse response) throws IOException {
		try {
			this.callback.call(response);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@FunctionalInterface
	public static interface Callback {
		public void call(HttpServletResponse resp) throws Exception;
	}

}
