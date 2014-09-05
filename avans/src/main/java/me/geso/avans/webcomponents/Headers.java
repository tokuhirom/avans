package me.geso.avans.webcomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import lombok.ToString;

/**
 * This class represents HTTP headers. This is *not* a thread safe.
 * 
 * @author tokuhirom
 *
 */
@ToString
public class Headers {
	// should be ordered. It makes testing easier.
	private Map<String, List<String>> map = new TreeMap<>();

	public void add(String key, String value) {
		key = key.toLowerCase();
		if (key.contains("\n")) {
			throw new RuntimeException(
					"You can't include new line character in header key.");
		}
		if (value.contains("\n")) {
			throw new RuntimeException(
					"You can't include new line character in header value.");
		}
		if (!map.containsKey(key)) {
			map.put(key, new ArrayList<String>());
		}
		map.get(key).add(value);
	}

	public List<String> getAll(String key) {
		key = key.toLowerCase();
		List<String> list = map.get(key);
		if (list == null) {
			list = new ArrayList<>();
		}
		return list;
	}

	public Optional<String> getFirst(String key) {
		key = key.toLowerCase();
		List<String> list = map.get(key);
		if (list != null && list.size() > 0) {
			return Optional.of(list.get(0));
		} else {
			return Optional.empty();
		}
	}

	public Set<String> keySet() {
		return map.keySet();
	}
	
	public Set<String> headerNames() {
		return this.map.keySet();
	}

	public void set(String key, String value) {
		key = key.toLowerCase();
		List<String> values = new ArrayList<String>();
		values.add(value);
		map.put(key, values);
	}

}
