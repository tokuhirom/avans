Distribution
============

avans supports war file as a primary choice. war file is a zip file, contains `*.class`, template files, and dependency libraries.
You can deploy war file on tomcat or other servlet containers.

## How do I create Executable WAR?

You can use winstone servlet engine. It provides really elegant maven plugin.

You need to add following lines into your pom.xml.

			<plugin>
				<groupId>net.sf.alchim</groupId>
				<artifactId>winstone-maven-plugin</artifactId>
				<executions>
					<execution>
						<goals>
							<goal>embed</goal>
						</goals>
						<phase>package</phase>
					</execution>
				</executions>
			</plugin>

