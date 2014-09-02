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
		// base/target/Foo-0.0.1-SNAPSHOT.jar
		// base/target/classes/
		if (path.getName(path.getNameCount()-1-1).equals("target")) {
			path = path.getParent().getParent();
		}
		return path;
	}
}
