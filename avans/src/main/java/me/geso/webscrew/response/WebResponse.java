package me.geso.webscrew.response;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * This is a interface for Response object.
 * 
 * @author tokuhirom
 *
 */
public interface WebResponse {
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
