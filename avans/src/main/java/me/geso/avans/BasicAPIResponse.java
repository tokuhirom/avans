package me.geso.avans;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class BasicAPIResponse {
	private int code;
	private List<String> messages;

	public BasicAPIResponse() {
		this.code = 200;
		this.messages = Collections.emptyList();
	}

	public BasicAPIResponse(int code, String message) {
		this.code = code;
		this.messages = Arrays.asList(message);
	}

	public BasicAPIResponse(int code, List<String> messages) {
		this.code = code;
		this.messages = messages;
	}

	public int getCode() {
		return this.code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public List<String> getMessages() {
		return this.messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

}
