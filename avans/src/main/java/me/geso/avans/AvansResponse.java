package me.geso.avans;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * This is a interface for Response object.
 * 
 * @author tokuhirom
 *
 */
public interface AvansResponse {
	/**
	 * Write the response to HttpServletResponse object.
	 * 
	 * @param response
	 * @throws IOException
	 */
	public void write(HttpServletResponse response) throws IOException;

	public void addHeader(String name, String value);

	public void setHeader(String name, String value);
}
