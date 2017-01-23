package me.geso.avans.jackson;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.geso.webscrew.response.ByteArrayResponse;

public class JacksonJsonViewTest {

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
        private List<JsonTest0Inner> list2;
        private Map<String, String> map0;
        private Map<String, Integer> map1;
        private Map<String, JsonTest0Inner> map2;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class JsonTest0Inner {
        private String name;
        private String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class JsonTest1 {
        private HashMap<String, HashMap<String, String>> map0;
    }

    ObjectReader readerJsonTest0 = new ObjectMapper().reader(JsonTest0.class);

    ObjectReader readerJsonTest1 = new ObjectMapper().reader(JsonTest1.class);

    @Test
    public void test0() throws Exception {

        JacksonJsonView sut = new JacksonJsonViewMock();

        {
            JsonTest0 jsonTest0 = new JsonTest0(Arrays.asList("a1", "b1"),
                                                Arrays.asList(1, 2, 3),
                                                Arrays.asList(
                                                        new JsonTest0Inner("aa", "bb"),
                                                        new JsonTest0Inner("cc", "dd")),
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
                                                new HashMap<String, JsonTest0Inner>() {
                                                    {
                                                        put("x", new JsonTest0Inner("x1", "x2"));
                                                        put("y", new JsonTest0Inner("y1", "y2"));
                                                    }
                                                });

            ByteArrayResponse response = (ByteArrayResponse) sut.renderJSON(jsonTest0);

            JsonTest0 actual = readerJsonTest0.readValue(response.getBody());

            assertThat(actual.getList0().size(), is(2));
            assertThat(actual.getList0().get(0), is("a1"));
            assertThat(actual.getList0().get(1), is("b1"));

            assertThat(actual.getList1().size(), is(3));
            assertThat(actual.getList1().get(0), is(1));
            assertThat(actual.getList1().get(1), is(2));
            assertThat(actual.getList1().get(2), is(3));

            assertThat(actual.getList2().size(), is(2));
            assertThat(actual.getList2().get(0).getName(), is("aa"));
            assertThat(actual.getList2().get(0).getValue(), is("bb"));
            assertThat(actual.getList2().get(1).getName(), is("cc"));
            assertThat(actual.getList2().get(1).getValue(), is("dd"));

            assertThat(actual.getMap0().size(), is(2));
            assertThat(actual.getMap0().get("ee"), is("ff"));
            assertThat(actual.getMap0().get("gg"), is("hh"));

            assertThat(actual.getMap1().size(), is(2));
            assertThat(actual.getMap1().get("ii"), is(10));
            assertThat(actual.getMap1().get("jj"), is(20));

            assertThat(actual.getMap2().size(), is(2));
            assertThat(actual.getMap2().get("x").getName(), is("x1"));
            assertThat(actual.getMap2().get("x").getValue(), is("x2"));
            assertThat(actual.getMap2().get("y").getName(), is("y1"));
            assertThat(actual.getMap2().get("y").getValue(), is("y2"));
        }

        {
            HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>() {
                {
                    put("xxx", new HashMap<String, String>() {
                        {
                            put("xxx1", "xxx2");
                        }
                    });
                    put("yyy", new HashMap<String, String>() {
                        {
                            put("yyy1", "yyy2");
                        }
                    });
                }
            };

            JsonTest1 jsonTest1 = new JsonTest1(map);

            ByteArrayResponse response = (ByteArrayResponse) sut.renderJSON(jsonTest1);

            JsonTest1 actual = readerJsonTest1.readValue(response.getBody());

            assertThat(actual.getMap0().size(), is(2));
            assertThat(actual.getMap0().get("xxx").size(), is(1));
            assertThat(actual.getMap0().get("xxx").get("xxx1"), is("xxx2"));
            assertThat(actual.getMap0().get("yyy").size(), is(1));
            assertThat(actual.getMap0().get("yyy").get("yyy1"), is("yyy2"));
        }
    }
}
