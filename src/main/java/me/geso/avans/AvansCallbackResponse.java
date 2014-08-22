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

	private int status;
	private Callback callback;

	public AvansCallbackResponse(@NonNull Callback callback) {
		this.status = 200;
		this.callback = callback;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public void write(HttpServletResponse response) throws IOException {
		response.setStatus(status);
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
