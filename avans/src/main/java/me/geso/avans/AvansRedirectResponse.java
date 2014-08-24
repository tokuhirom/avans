package me.geso.avans;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class AvansRedirectResponse implements AvansResponse {

	private String location;

	public AvansRedirectResponse(String location) {
		this.location = location;
	}

	@Override
	public void write(HttpServletResponse response) throws IOException {
		response.sendRedirect(location);
	}

}
