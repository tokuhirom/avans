package me.geso.avans;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AvansUtil {

	/**
	 * Get project base directory. TODO: better jar location detection
	 * algorithm.
	 * 
	 * @return
	 */
	public static Path getBaseDirectory(final Class<?> klass) {
		final String baseDirectory = klass.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		Path path = Paths.get(baseDirectory);
		// base/target/Foo-0.0.1-SNAPSHOT.jar
		// base/target/classes/
		if (path.getName(path.getNameCount() - 1 - 1).toString()
				.equals("target")) {
			path = path.getParent().getParent();
		}
		return path;
	}

}
