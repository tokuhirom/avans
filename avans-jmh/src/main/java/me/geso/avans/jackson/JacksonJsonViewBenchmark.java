package me.geso.avans.jackson;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class JacksonJsonViewBenchmark {

    public static class JacksonJsonViewMock implements JacksonJsonView {
        @Override
        public void close() throws Exception {
        }

        @Override
        public Optional<Object> getPluginStashValue(Class<?> pluginClass, String key) {
            return null;
        }

        @Override
        public Object computePluginStashValueIfAbsent(Class<?> pluginClass, String key,
                                                      Supplier<?> supplier) {
            return null;
        }

        @Override
        public void setPluginStashValue(Class<?> pluginClass, String key, Object value) {
        }

        @Override
        public void init(HttpServletRequest request, HttpServletResponse response,
                         Map<String, String> captured) {
        }

        @Override
        public HttpServletRequest getServletRequest() {
            return null;
        }

        @Override
        public Map<String, String> getPathParams() {
            return null;
        }

        @Override
        public void invoke(final Method method,
                           final HttpServletRequest servletRequest,
                           final HttpServletResponse servletResponse,
                           final Map<String, String> captured) {
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class JsonTest0 {
        private List<String> list0;
        private List<Integer> list1;
        private List<JsonTest1> list2;
        private Map<String, String> map0;
        private Map<String, Integer> map1;
        private Map<String, JsonTest1> map2;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class JsonTest1 {
        private String name;
        private String value;
    }

    @Benchmark
    public void bench(Blackhole blackhole) {

        IntStream.rangeClosed(0, 10_000)
                 .forEach(i -> {
                              JsonTest0 jsonTest0 = new JsonTest0(Arrays.asList("a1", "b1"),
                                                                  Arrays.asList(1, 2, 3),
                                                                  Arrays.asList(
                                                                          new JsonTest1("aa", "bb"),
                                                                          new JsonTest1("cc", "dd")),
                                                                  new HashMap<String, String>() {
                                                                      {
                                                                          put("ee", "ff");
                                                                          put("gg", "hh");
                                                                      }
                                                                  },
                                                                  new HashMap<String, Integer>() {
                                                                      {
                                                                          put("ii", 10);
                                                                          put("jj", 20);
                                                                      }
                                                                  },
                                                                  new HashMap<String, JsonTest1>() {
                                                                      {
                                                                          put("x", new JsonTest1("x1", "x2"));
                                                                          put("y", new JsonTest1("y1", "y2"));
                                                                      }
                                                                  });

                              JacksonJsonView sut = new JacksonJsonViewMock();

                              blackhole.consume(sut.renderJSON(jsonTest0));
                          }
                 );
    }
}
