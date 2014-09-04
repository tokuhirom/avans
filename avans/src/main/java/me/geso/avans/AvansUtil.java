package me.geso.avans;

import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import lombok.SneakyThrows;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.MultiValueMap;

public class AvansUtil {

	/**
	 * Get project base directory. TODO: better jar location detection
	 * algorithm.
	 * 
	 * @return
	 */
	@SneakyThrows
	public static Path getBaseDirectory(Class<?> klass) {
		String baseDirectory = klass.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		Path path = Paths.get(baseDirectory);
		// base/target/Foo-0.0.1-SNAPSHOT.jar
		// base/target/classes/
		if (path.getName(path.getNameCount() - 1 - 1).toString()
				.equals("target")) {
			path = path.getParent().getParent();
		}
		return path;
	}

	@SneakyThrows
	public static Parameters parseQueryString(String queryString,
			String encoding) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		MultiValueMap<String, String> query =
				MapUtils.multiValueMap(new LinkedHashMap(), LinkedHashSet.class);
		if (queryString != null) {
			for (String pair : queryString.split("&")) {
				int eq = pair.indexOf("=");
				if (eq < 0) {
					// key with no value
					query.put(URLDecoder.decode(pair, encoding), "");
				} else {
					// key=value
					String key = URLDecoder.decode(pair.substring(0, eq),
							encoding);
					String value = URLDecoder.decode(pair.substring(eq + 1),
							encoding);
					query.put(key, value);
				}
			}
		}
		return new Parameters(query);
	}

}
