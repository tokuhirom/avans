package me.geso.avans;

import java.util.Map;
import java.util.Optional;

/**
 * The class represents path paremeters.
 * 
 * @author tokuhirom
 *
 */
public class AvansPathParameters {
	private final Map<String, String> map;

	public AvansPathParameters(Map<String, String> map) {
		this.map = map;
	}

	public String get(String name) {
		if (!map.containsKey(name)) {
			throw new RuntimeException("Missing mandatory path parameter: "
					+ name);
		}
		return map.get(name);
	}

	/**
	 * Get a path parameter in long.
	 * 
	 * @param name
	 * @return
	 */
	public long getLong(String name) {
		String arg = this.get(name);
		return Long.parseLong(arg);
	}

	/**
	 * Get a path parameter in int.
	 * 
	 * @param name
	 * @return
	 */
	public int getInt(String name) {
		String arg = this.get(name);
		return Integer.parseInt(arg);
	}

	/**
	 * Get a path parameter in String. But this doesn't throws exception if the
	 * value doesn't exists.
	 * 
	 * @param name
	 * @return
	 */
	public Optional<String> getOptionalArg(String name) {
		if (this.map.containsKey(name)) {
			return Optional.of(map.get(name));
		} else {
			return Optional.empty();
		}
	}
}
