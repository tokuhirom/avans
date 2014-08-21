package me.geso.avans;

import lombok.SneakyThrows;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractController {

	@SneakyThrows
	public AvansResponse renderJSON(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);

		byte[] json = mapper.writeValueAsBytes(obj);
		AvansBytesResponse res = new AvansBytesResponse();
		res.setContentType("application/json; charset=utf-8");
		res.setContentLength(json.length);
		res.setBody(json);
		return res;
	}
}
