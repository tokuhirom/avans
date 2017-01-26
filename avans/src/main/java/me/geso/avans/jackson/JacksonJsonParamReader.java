package me.geso.avans.jackson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import me.geso.avans.JSONParamReader;

public interface JacksonJsonParamReader extends JSONParamReader {

	class _PrivateStaticFields {
		// only accessible in this interface.
		private static ObjectReader _reader = createObjectReader();
		private static Map<Class<?>, ObjectReader> _readerMap = new ConcurrentHashMap<>();

		private static ObjectReader createObjectReader() {
			return new ObjectMapper()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.reader();
		}
	}

	/**
	 * If you want to customize this behavior, you can copy and paste this code
	 * into your code.
	 */
	@Override
	public default Object readJsonParam(InputStream is, Class<?> valueType)
			throws IOException {
		// Read bytes into byte array.
		// It needs for better logging, error introspection.
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024 * 4];
		int n;
		while (-1 != (n = is.read(buffer))) {
			output.write(buffer, 0, n);
		}
		final byte[] byteArray = output.toByteArray();

		try {
			ObjectReader reader = _PrivateStaticFields._readerMap
					.computeIfAbsent(valueType, key -> _PrivateStaticFields._reader.withType(key));
			return reader.readValue(byteArray);
		} catch (JsonParseException | JsonMappingException e) {
			final String json = new String(byteArray,
				StandardCharsets.UTF_8);
			final Throwable cause = e.getCause();
			final String message = ((cause != null ? cause.getMessage()
				: e.getMessage())
				+ " : " + json);
			throw new IOException(message, e);
		}
	}
}
