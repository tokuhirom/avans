package me.geso.avans.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.geso.avans.Controller;
import me.geso.avans.JSONRendererProvider;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.WebResponse;

public interface JacksonJsonView extends Controller, JSONRendererProvider {

	class _PrivateStaticFields {
		// only accessible in this interface.
		private static ObjectMapper _mapper = createObjectMapper();

		private static ObjectMapper createObjectMapper() {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			mapper.getFactory().setCharacterEscapes(new CharacterEscapesAgainstXSS());
			return mapper;
		}
	}

	@Override
	public default WebResponse renderJSON(final int statusCode, final Object obj) {
		byte[] json;
		try {
			json = _PrivateStaticFields._mapper.writeValueAsBytes(obj);
		} catch (final JsonProcessingException e) {
			// It caused by programming error.
			throw new RuntimeException(e);
		}

		final ByteArrayResponse res = new ByteArrayResponse(statusCode, json);
		res.setContentType("application/json; charset=utf-8");
		res.setContentLength(json.length);
		return res;
	}

	/**
	 * Rendering JSON by jackson.
	 *
	 * @param obj
	 * @return
	 */
	public default WebResponse renderJSON(final Object obj) {
		return this.renderJSON(200, obj);
	}
}
