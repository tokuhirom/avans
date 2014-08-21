package me.geso.avans;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class AvansAPIResponse<T extends Object> {
	@Getter
	@Setter
	int code;
	@Getter
	@Setter
	List<String> messages;
	@Getter
	@Setter
	T data;

	public AvansAPIResponse(T data) {
		this.code = 200;
		this.messages = new ArrayList<>();
		this.data = data;
	}

	public AvansAPIResponse(int code, T data) {
		this.code = code;
		this.messages = new ArrayList<>();
		this.data = data;
	}

	public void setMessage(String message) {
		this.messages = new ArrayList<>();
		this.messages.add(message);
	}

}
