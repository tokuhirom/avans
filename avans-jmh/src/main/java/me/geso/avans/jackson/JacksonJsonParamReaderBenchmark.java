package me.geso.avans.jackson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class JacksonJsonParamReaderBenchmark {

    @Benchmark
    public void bench(Blackhole blackhole) throws Exception {
        // resolve testdata file
        String classFileName = "/" + getClass().getName().replaceAll("\\.", "/") + ".class";
        String classFilePath = getClass().getResource(classFileName).getFile().replaceAll("file:", "");
        File jarFilePath = new File(classFilePath.replaceFirst("!/.*$", ""));
        Path dataFilePath = Paths.get(jarFilePath.getParentFile().getParentFile() +
                                      "/src/main/resources/me/geso/avans/jackson/data1.json");
        File dataFile = dataFilePath.toFile();

        IntStream.rangeClosed(0, 10_000)
                 .forEach(i -> {
                              try (FileInputStream path = new FileInputStream(dataFile)) {
                                  JacksonJsonParamReader sut = new JacksonJsonParamReader() {};
                                  blackhole.consume(
                                          sut.readJsonParam(path, JacksonJsonViewBenchmark.JsonTest0.class));
                              } catch (IOException e) {
                                  throw new UncheckedIOException(e);
                              }
                          }
                 );
    }
}
