package me.geso.avans;

import lombok.NonNull;
import me.geso.webscrew.response.WebResponse;

public class ParameterProcessorResult {
	private WebResponse webResponse;
	private Object data;
	private String missingParameter;

	/**
	 * Creating parameter processor result from WebResponse object.
	 * 
	 * @param response
	 * @return
	 */
	public static ParameterProcessorResult fromWebResponse(
			@NonNull WebResponse response) {
		final ParameterProcessorResult p = new ParameterProcessorResult();
		p.webResponse = response;
		return p;
	}

	/**
	 * Use this method if you got a data from parameter.
	 * 
	 * @param data
	 * @return
	 */
	public static ParameterProcessorResult fromData(
			@NonNull Object data) {
		final ParameterProcessorResult p = new ParameterProcessorResult();
		p.data = data;
		return p;
	}

	/**
	 * Use this method if you got a data from parameter.
	 * 
	 * @param data
	 * @return
	 */
	public static ParameterProcessorResult missingParameter(
			@NonNull String name) {
		final ParameterProcessorResult p = new ParameterProcessorResult();
		p.missingParameter = name;
		return p;
	}

	/**
	 * Return true if the result contains WebResponse object.
	 * 
	 * @return
	 */
	public boolean hasResponse() {
		return this.webResponse != null;
	}

	public WebResponse getResponse() {
		if (this.webResponse == null) {
			throw new NullPointerException();
		}
		return this.webResponse;
	}

	public boolean hasData() {
		return this.data != null;
	}

	public Object getData() {
		if (this.data == null) {
			throw new NullPointerException();
		}
		return this.data;
	}

	public boolean hasMissingParameter() {
		return this.missingParameter != null;
	}

	public String getMissingParameter() {
		if (this.missingParameter == null) {
			throw new NullPointerException();
		}
		return this.missingParameter;
	}
}