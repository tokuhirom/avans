package me.geso.webscrew;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.apache.commons.collections4.MultiMap;

/**
 * The class represents path paremeters.
 * 
 * @author tokuhirom
 *
 */
public class Parameters {
	@Override
	public String toString() {
		return "Parameters [map=" + map + "]";
	}

	private final MultiMap<String, String> map;

	public Parameters(MultiMap<String, String> map) {
		this.map = map;
	}

	public String get(String name) {
		if (!map.containsKey(name)) {
			throw new RuntimeException("Missing mandatory path parameter: "
					+ name);
		}

		@SuppressWarnings("unchecked")
		Collection<String> collection = (Collection<String>) map.get(name);
		return collection.iterator().next();
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

	public double getDouble(String name) {
		String arg = this.get(name);
		return Double.parseDouble(arg);
	}

	public OptionalInt getOptionalInt(String name) {
		Optional<String> arg = this.getOptional(name);
		if (arg.isPresent()) {
			return OptionalInt.of(Integer.parseInt(arg.get()));
		} else {
			return OptionalInt.empty();
		}
	}

	public OptionalLong getOptionalLong(String name) {
		Optional<String> arg = this.getOptional(name);
		if (arg.isPresent()) {
			return OptionalLong.of(Long.parseLong(arg.get()));
		} else {
			return OptionalLong.empty();
		}
	}

	public OptionalDouble getOptionalDouble(String name) {
		Optional<String> arg = this.getOptional(name);
		if (arg.isPresent()) {
			return OptionalDouble.of(Double.parseDouble(arg.get()));
		} else {
			return OptionalDouble.empty();
		}
	}

	/**
	 * Get a path parameter in String. But this doesn't throws exception if the
	 * value doesn't exists.
	 * 
	 * @param name
	 * @return
	 */
	public Optional<String> getOptional(String name) {
		@SuppressWarnings("unchecked")
		Collection<String> collection = (Collection<String>) map.get(name);
		if (collection == null) {
			return Optional.empty();
		}
		return collection.stream().findFirst();
	}
	
	public boolean containsKey(String name) {
		@SuppressWarnings("unchecked")
		Collection<String> collection = (Collection<String>) map.get(name);
		if (collection == null) {
			return false;
		}
		if (collection.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}


}
