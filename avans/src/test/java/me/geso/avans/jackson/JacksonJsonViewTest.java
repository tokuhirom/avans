package me.geso.avans.jackson;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.geso.avans.ControllerBase;
import me.geso.avans.jackson.JacksonJsonView._PrivateStaticFields;
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

    public static class HttpServletRequestMock implements HttpServletRequest {

        @Override
        public String getAuthType() {
            return null;
        }

        @Override
        public Cookie[] getCookies() {
            return new Cookie[0];
        }

        @Override
        public long getDateHeader(String name) {
            return 0;
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return null;
        }

        @Override
        public int getIntHeader(String name) {
            return 0;
        }

        @Override
        public String getMethod() {
            return null;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public String getPathTranslated() {
            return null;
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public String getRemoteUser() {
            return null;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            return null;
        }

        @Override
        public String getRequestURI() {
            return null;
        }

        @Override
        public StringBuffer getRequestURL() {
            return null;
        }

        @Override
        public String getServletPath() {
            return null;
        }

        @Override
        public HttpSession getSession(boolean create) {
            return null;
        }

        @Override
        public HttpSession getSession() {
            return null;
        }

        @Override
        public String changeSessionId() {
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            return false;
        }

        @Override
        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
            return false;
        }

        @Override
        public void login(String username, String password) throws ServletException {
        }

        @Override
        public void logout() throws ServletException {
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            return null;
        }

        @Override
        public Part getPart(String name) throws IOException, ServletException {
            return null;
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
                throws IOException, ServletException {
            return null;
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;
        }

        @Override
        public String getCharacterEncoding() {
            return null;
        }

        @Override
        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        }

        @Override
        public int getContentLength() {
            return 0;
        }

        @Override
        public long getContentLengthLong() {
            return 0;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return null;
        }

        @Override
        public String getParameter(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return null;
        }

        @Override
        public String[] getParameterValues(String name) {
            return new String[0];
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return null;
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public String getScheme() {
            return null;
        }

        @Override
        public String getServerName() {
            return null;
        }

        @Override
        public int getServerPort() {
            return 0;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public String getRemoteHost() {
            return null;
        }

        @Override
        public void setAttribute(String name, Object o) {
        }

        @Override
        public void removeAttribute(String name) {
        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public Enumeration<Locale> getLocales() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        @Override
        public String getRealPath(String path) {
            return null;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public String getLocalName() {
            return null;
        }

        @Override
        public String getLocalAddr() {
            return null;
        }

        @Override
        public int getLocalPort() {
            return 0;
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            return null;
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
                throws IllegalStateException {
            return null;
        }

        @Override
        public boolean isAsyncStarted() {
            return false;
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public AsyncContext getAsyncContext() {
            return null;
        }

        @Override
        public DispatcherType getDispatcherType() {
            return null;
        }
    }

    public static class HttpServletResponseMock implements HttpServletResponse {

        @Override
        public void addCookie(Cookie cookie) {
        }

        @Override
        public boolean containsHeader(String name) {
            return false;
        }

        @Override
        public String encodeURL(String url) {
            return null;
        }

        @Override
        public String encodeRedirectURL(String url) {
            return null;
        }

        @Override
        public String encodeUrl(String url) {
            return null;
        }

        @Override
        public String encodeRedirectUrl(String url) {
            return null;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
        }

        @Override
        public void sendError(int sc) throws IOException {
        }

        @Override
        public void sendRedirect(String location) throws IOException {
        }

        @Override
        public void setDateHeader(String name, long date) {
        }

        @Override
        public void addDateHeader(String name, long date) {
        }

        @Override
        public void setHeader(String name, String value) {
        }

        @Override
        public void addHeader(String name, String value) {
        }

        @Override
        public void setIntHeader(String name, int value) {
        }

        @Override
        public void addIntHeader(String name, int value) {
        }

        @Override
        public void setStatus(int sc) {
        }

        @Override
        public void setStatus(int sc, String sm) {
        }

        @Override
        public int getStatus() {
            return 0;
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return null;
        }

        @Override
        public Collection<String> getHeaderNames() {
            return null;
        }

        @Override
        public String getCharacterEncoding() {
            return null;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return null;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return null;
        }

        @Override
        public void setCharacterEncoding(String charset) {
        }

        @Override
        public void setContentLength(int len) {
        }

        @Override
        public void setContentLengthLong(long len) {
        }

        @Override
        public void setContentType(String type) {
        }

        @Override
        public void setBufferSize(int size) {
        }

        @Override
        public int getBufferSize() {
            return 0;
        }

        @Override
        public void flushBuffer() throws IOException {
        }

        @Override
        public void resetBuffer() {
        }

        @Override
        public boolean isCommitted() {
            return false;
        }

        @Override
        public void reset() {
        }

        @Override
        public void setLocale(Locale loc) {
        }

        @Override
        public Locale getLocale() {
            return null;
        }
    }

    public static class ControllerMock extends ControllerBase {

        @Override
        public ObjectMapper createObjectMapper() {
            return new ObjectMapper();
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

    @Test
    public void testOverridableObjectMapper() throws Exception {

        _PrivateStaticFields privateStaticFields = new _PrivateStaticFields();
        Class<?> clazz = privateStaticFields.getClass();
        Field filed = clazz.getDeclaredField("_writer");
        filed.setAccessible(true);

        {
            // default ObjectMapper
            ControllerBase sut = new ControllerBase() {};
            sut.init(new HttpServletRequestMock(), new HttpServletResponseMock(), new HashMap<>());
            sut.renderJSON(new HashMap<>());
            sut.init(new HttpServletRequestMock(), new HttpServletResponseMock(), new HashMap<>());
            sut.renderJSON(new HashMap<>());

            // Is the default ObjectWriter?
            ObjectWriter writer = (ObjectWriter) filed.get(privateStaticFields);
            JsonFactory factory = writer.getFactory();
            boolean actual = factory.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII);
            assertThat(actual, is(true));

            // Is the same instance after re-construct?
            sut = new ControllerBase() {};
            sut.init(new HttpServletRequestMock(), new HttpServletResponseMock(), new HashMap<>());
            sut.renderJSON(new HashMap<>());
            sut.init(new HttpServletRequestMock(), new HttpServletResponseMock(), new HashMap<>());
            sut.renderJSON(new HashMap<>());
            ObjectWriter newWriter = (ObjectWriter) filed.get(privateStaticFields);
            assertThat(newWriter, is(writer));
        }

        {
            // customized ObjectMapper
            ControllerMock sut = new ControllerMock();
            sut.init(new HttpServletRequestMock(), new HttpServletResponseMock(), new HashMap<>());
            sut.renderJSON(new HashMap<>());
            sut.init(new HttpServletRequestMock(), new HttpServletResponseMock(), new HashMap<>());
            sut.renderJSON(new HashMap<>());

            // Was the ObjectWriter Overrided?
            ObjectWriter writer = (ObjectWriter) filed.get(privateStaticFields);
            JsonFactory factory = writer.getFactory();
            boolean isEnabled = factory.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII);
            assertThat(isEnabled, is(false));

            // Is the same instance after re-construct?
            sut = new ControllerMock();
            sut.init(new HttpServletRequestMock(), new HttpServletResponseMock(), new HashMap<>());
            sut.renderJSON(new HashMap<>());
            sut.init(new HttpServletRequestMock(), new HttpServletResponseMock(), new HashMap<>());
            sut.renderJSON(new HashMap<>());
            ObjectWriter newWriter = (ObjectWriter) filed.get(privateStaticFields);
            assertThat(newWriter, is(writer));
        }
    }
}
