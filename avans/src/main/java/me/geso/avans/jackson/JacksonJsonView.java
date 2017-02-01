package me.geso.avans.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import me.geso.avans.Controller;
import me.geso.avans.JSONRendererProvider;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.WebResponse;

public interface JacksonJsonView extends Controller, JSONRendererProvider {

	class _PrivateStaticFields {
		// only accessible in this interface.
		private static final ObjectMapper _defaultMapper;
		private static ObjectMapper _mapper;
		private static ObjectWriter _writer;

		static {
			ObjectMapper objectMapper = createObjectMapper();
			_defaultMapper = objectMapper;
			_mapper = objectMapper;
			_writer = objectMapper.writer();
		}

		private static ObjectMapper createObjectMapper() {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			mapper.getFactory().setCharacterEscapes(new CharacterEscapesAgainstXSS());
			return mapper;
		}
	}

	@Override
	public default WebResponse renderJSON(final int statusCode, final Object obj) {
		// If overrided createObjectMapper method, replace the writer only once
		ObjectMapper objectMapper = createObjectMapper();
		if (objectMapper != null && _PrivateStaticFields._mapper == _PrivateStaticFields._defaultMapper) {
			_PrivateStaticFields._mapper = objectMapper;
			_PrivateStaticFields._writer = objectMapper.writer();
		}

		byte[] json;
		try {
			json = _PrivateStaticFields._writer.writeValueAsBytes(obj);
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

	public default ObjectMapper createObjectMapper() {
		return null;
	}
}
