package me.geso.avans;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

// TODO: Implement Avans Callback Response
public interface AvansResponse {
	public void setStatus(int status);
	public void write(HttpServletResponse response) throws IOException;
}
