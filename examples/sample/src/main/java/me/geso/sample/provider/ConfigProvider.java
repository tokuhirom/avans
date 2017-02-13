package me.geso.sample.provider;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Provider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.geso.sample.config.Config;

public class ConfigProvider implements Provider<Config> {
	@Override
	public Config get() {
		String env = System.getProperty("sample.env");
		if (env == null) {
			//throw new RuntimeException("Missing sample.env");
			env = "local";
		} 
		String fileName = "config/" + env + ".yml";
		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
			fileName)) {
			if (stream == null) {
				throw new RuntimeException("Cannot load " + fileName
					+ " from resource.");
			}
			try {
				return new ObjectMapper(new YAMLFactory()).readValue(stream, Config.class);
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
