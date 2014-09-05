package me.geso.avans;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@ToString
public class ValidationException extends RuntimeException {
	@Getter
	private final List<String> messages;

	public ValidationException(List<String> messages) {
		this.messages = messages;
	}

	private static final long serialVersionUID = 1L;

}
