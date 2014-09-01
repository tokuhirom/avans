package me.geso.avans;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class AvansAPIResponse<T extends Object> {
	@Getter
	@Setter
	int code;

	@Getter
	List<String> messages;

	@Getter
	@Setter
	T data;

	/**
	 * Create new instance.
	 */
	public AvansAPIResponse() {
		this.code = 200;
		this.messages = new ArrayList<>();
		this.data = null;
	}

	/**
	 * Create new instance with the data. Default status code is 200.
	 */
	public AvansAPIResponse(T data) {
		this.code = 200;
		this.messages = new ArrayList<>();
		this.data = data;
	}

	/**
	 * Create new instance.
	 */
	public AvansAPIResponse(int code, T data) {
		this.code = code;
		this.messages = new ArrayList<>();
		this.data = data;
	}

	/**
	 * Create new instance.
	 */
	public AvansAPIResponse(int code, String message, T data) {
		this.code = code;
		this.messages = new ArrayList<>();
		this.messages.add(message);
		this.data = data;
	}

	/**
	 * Set message. Current message will remove.
	 */
	public void setMessage(String message) {
		this.messages = new ArrayList<>();
		this.messages.add(message);
	}

	/**
	 * Set messages. Current message will remove.
	 */
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	/**
	 * Create empty response
	 */
	public static AvansAPIResponse<String> empty() {
		return new AvansAPIResponse<String>(null);
	}

	/**
	 * Create 404 not found response
	 */
	public static AvansAPIResponse<String> notFound() {
		return new AvansAPIResponse<String>(404, "Resource Not Found", null);
	}

	/**
	 * Create 403 forbidden API response
	 */
	public static AvansAPIResponse<String> Forbidden() {
		return new AvansAPIResponse<String>(403, "Forbidden", null);
	}
}
