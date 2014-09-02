package me.geso.avans;

import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;

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
		if (path.endsWith(Paths.get("target", "classes"))) {
			path = path.resolve("../..");
		}
		return path;
	}
}
