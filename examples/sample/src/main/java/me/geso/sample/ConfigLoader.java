package me.geso.sample;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;

public enum ConfigLoader {
	INSTANCE;

	private final Config config;

	private ConfigLoader() {
		String env = System.getProperty("sample.env");
		if (env == null) {
			throw new RuntimeException("Missing sample.env");
		} else {
			String fileName = "config/" + env + ".yml";
			try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
					fileName)) {
				try {
					this.config = new ObjectMapper(new YAMLFactory()).readValue(stream, Config.class);
				} catch (JsonParseException | JsonMappingException e) {
					throw new RuntimeException(String.format("Cannot parse %s: %s",
							fileName, e.getMessage()));
				}
			} catch (IOException e) {
				throw new RuntimeException(String.format("Cannot read %s from resources: %s",
						fileName, e.getMessage()));
			}
		}
	}

	public static Config getConfig() {
		return ConfigLoader.INSTANCE.config;
	}
}
