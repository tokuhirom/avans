#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.provider;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Provider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import ${package}.config.Config;

public class ConfigProvider implements Provider<Config> {
	@Override
	public Config get() {
		String env = System.getProperty("${artifactId}.env");
		if (env == null) {
			throw new RuntimeException("Missing ${artifactId}.env");
		} else {
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
}
