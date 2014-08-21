package me.geso.avans;

import java.util.Map;

@FunctionalInterface
public interface AvansAction {
	public AvansResponse run(AvansWebApplication web, Map<String, String> captured);
}
