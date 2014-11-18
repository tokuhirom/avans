package me.geso.avans;

import java.io.IOException;
import java.io.InputStream;

/**
 * Read {@literal @JsonParam} value from InputStream.
 */
public interface JSONParamReader {
	public Object readJsonParam(InputStream is, Class<?> type)
			throws IOException;
}
