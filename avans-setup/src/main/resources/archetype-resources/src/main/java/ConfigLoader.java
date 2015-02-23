#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

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
		String env = System.getProperty("${artifactId}.env");
		if (env == null) {
			throw new RuntimeException("Missing ${artifactId}.env");
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
