package me.geso.avans.jackson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import me.geso.avans.JSONParamReader;

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

		// Read bytes into byte array.
		// It needs for better logging, error introspection.
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while (-1 != (n = is.read(buffer))) {
			output.write(buffer, 0, n);
		}
		final byte[] byteArray = output.toByteArray();

		try {
			final Object value = mapper.readValue(byteArray, valueType);
			return value;
		} catch (JsonParseException | JsonMappingException e) {
			final String json = new String(byteArray,
				StandardCharsets.UTF_8);
			final Throwable cause = e.getCause();
			throw new IOException((cause != null ? cause.getMessage()
				: e.getMessage())
				+ " : " + json);
		}
	}
}
