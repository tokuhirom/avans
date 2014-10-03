package me.geso.avans.session.spymemcached;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class named "K".
 */
public class K {
	/**
	 * Create new LinkedHashMap from list of Objects.
	 * 
	 * @param objects
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <Key, Val> Map<Key, Val> map(Object... objects) {
		if (objects.length % 2 != 0) {
			throw new IllegalArgumentException(
					"arguments should be even number");
		}
		LinkedHashMap<Key, Val> map = new LinkedHashMap<>(
				objects.length / 2);
		for (int i = 0; i < objects.length; i += 2) {
			map.put((Key) objects[i], (Val) objects[i + 1]);
		}
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Create new immutable pair object. The immutable pair object implements
	 * {@code Map.Entry}.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <A, B> ImmutablePair<A, B> pair(A a, B b) {
		return new ImmutablePair<>(a, b);
	}

	public static class ImmutablePair<A, B> implements Map.Entry<A, B> {
		private A a;
		private B b;

		public ImmutablePair(A a, B b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public A getKey() {
			return this.a;
		}

		@Override
		public B getValue() {
			return this.b;
		}

		@Override
		public B setValue(B value) {
			throw new IllegalStateException(
					"This is a immutable object. You can't set value after creation.");
		}
	}

	/**
	 * Create new immutable list.
	 *
	 * @param ts
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> list(T... ts) {
		List<T> list = new ArrayList<>();
		for (T t : ts) {
			list.add(t);
		}
		return Collections.unmodifiableList(list);
	}
}
