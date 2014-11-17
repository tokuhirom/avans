package me.geso.avans.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import me.geso.avans.JSONParamReader;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface JacksonJsonParamReader extends JSONParamReader {

	/**
	 * If you want to customize this behavior, you can copy and paste this code
	 * into your code.
	 */
	@Override
	public default Object readJsonParam(InputStream is, Class<?> valueType)
			throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		// Ignore unknown properties.
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		final byte[] byteArray = IOUtils.toByteArray(is);
		try {
			final Object value = mapper.readValue(byteArray, valueType);
			return value;
		} catch (JsonParseException | JsonMappingException e) {
			final String json = new String(byteArray,
					StandardCharsets.UTF_8);
			throw new IOException(e.getCause().getMessage() + " : " + json);
		}
	}

}
