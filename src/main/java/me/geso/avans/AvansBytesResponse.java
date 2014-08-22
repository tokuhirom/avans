package me.geso.avans;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class AvansBytesResponse implements AvansResponse {
	@Getter
	@Setter
	AvansHeaders headers = new AvansHeaders();
	@Getter
	@Setter
	private int status = 200;
	@Getter
	@Setter
	private byte[] body;

	public void write(HttpServletResponse response) throws IOException {
		response.setStatus(status);
		for (String key : headers.keySet()) {
			for (String value : headers.getAll(key)) {
				response.addHeader(key, value);
			}
		}
		try (OutputStream os = response.getOutputStream()) {
			os.write(body);
		}
	}

	public void setContentType(String contentType) {
		headers.add("Content-Type", contentType);
	}

	public void setContentLength(long length) {
		headers.add("Content-Length", ""+length);
	}
}
