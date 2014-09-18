#!/usr/bin/env ruby

require 'fileutils'

class String
  def squish
    self.gsub(/^ {6}/, '')
  end
end

class AvansSetup
  def initialize(pkg)
    @pkg = pkg
    if pkg =~ /\A(.*)\.(.*?)\z/
      @groupId = $1
      @artifactId = $2
      @project = $2
    else
      raise "Invalid package name: #{pkg}"
    end
  end

  def run()
    if File.directory?(@project)
      raise "Directory is already exists: #{@project}/"
    end

    javaDir = "#{@project}/src/main/java/#{@pkg.gsub(/\./, '/')}"

    FileUtils.mkdir_p(javaDir)
    FileUtils.mkdir_p("#{javaDir}/controller/")
    FileUtils.mkdir_p("#{@project}/src/main/resources/")
    FileUtils.mkdir_p("#{@project}/templates/")

    File.open("#{javaDir}/Main.java", 'w') do |f|
      f.puts <<-"...".squish
      package #{@pkg};

      import org.eclipse.jetty.server.Server;

      import me.geso.avans.jetty.JettyServerBuilder;

      public class Main {
        public static void main(String[] args) throws Exception {
          Server server = new JettyServerBuilder()
              .setPort(21110)
              .registerPackage("#{@pkg}.controller")
              .build();
          server.start();
          server.join();
        }
      }
      ...
    end

    File.open("#{javaDir}/controller/RootController.java", 'w') do |f|
      f.puts <<-"...".squish
      package #{@pkg};

      import me.geso.avans.ControllerBase;
      import me.geso.avans.annotation.GET;
      import me.geso.webscrew.response.WebResponse;

      public static class RootController extends ControllerBase {
        @GET("/")
        public WebResponse index() {
          return this.renderMustache("index.mustache", null);
        }
      }
      ...
    end

    File.open("#{@project}/templates/index.mustache", 'w') do |f|
      f.puts <<-EOH.squish
      <!doctype html>
      <html>
        <body style="font-family:'Lucida Grande','Hiragino Kaku Gothic ProN', Meiryo, sans-serif; text-align: center; font-size: 900%; background-color: #feefee; color: #030303; vertical-align: middle;">
          Hello, world!
        </body>
      </html>
EOH
    end

    File.open("#{@project}/pom.xml", 'w') do |f|
      f.puts <<-"...".squish
      <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

        <modelVersion>4.0.0</modelVersion>
        <groupId>#{@groupId}</groupId>
        <artifactId>#{@artifactId}</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <name>#{@artifactId}</name>
        <description></description>

        <properties>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
          <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        </properties>

        <repositories>
          <repository>
            <id>tokuhirom</id>
            <url>https://tokuhirom.github.io/maven/releases/</url>
          </repository>
        </repositories>

        <dependencies>
          <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.11</version>
            <scope>test</scope>
          </dependency>
          <dependency>
            <groupId>me.geso</groupId>
            <artifactId>avans</artifactId>
            <version>0.15.1</version>
          </dependency>
          <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>1.7.7</version>
          </dependency>
        </dependencies>

        <build>
          <plugins>
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-compiler-plugin</artifactId>
              <version>3.1</version>
              <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <encoding>UTF-8</encoding>
                <compilerArgument>-parameters</compilerArgument>
              </configuration>
            </plugin>
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-shade-plugin</artifactId>
              <version>1.6</version>
              <configuration>
                <createDependencyReducedPom>true</createDependencyReducedPom>
                <filters>
                  <filter>
                    <artifact>*:*</artifact>
                    <excludes>
                      <exclude>META-INF/*.SF</exclude>
                      <exclude>META-INF/*.DSA</exclude>
                      <exclude>META-INF/*.RSA</exclude>
                    </excludes>
                  </filter>
                </filters>
              </configuration>
              <executions>
                <execution>
                  <phase>package</phase>
                  <goals>
                    <goal>shade</goal>
                  </goals>
                  <configuration>
                    <transformers>
                      <transformer
                        implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
                      <transformer
                        implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>#{@pkg}.Main</mainClass>
                      </transformer>
                    </transformers>
                  </configuration>
                </execution>
              </executions>
            </plugin>
          </plugins>
        </build>
      </project>
      ...
    end
    File.open("#{@project}/README.md", 'w') do |f|
      f.puts <<-"...".squish
      # #{@project}

      ## debug run

          mvn exec:java -DmainClass=#{@pkg}.Main

      ## make jar

          mvn package

      ## run program
      
          java -jar target/#{@project}-0.0.1-SNAPSHOT.jar

      ...
    end

    Dir.chdir(@project)
    system('mvn package')

    File.open('README.md') { |f|
      puts f.read
    }
  end
end

if ARGV.length != 1
  puts "Usage: avans-setup.rb package.name"
  exit
end

packageName = ARGV[0]

setup = AvansSetup.new(packageName)
setup.run()

