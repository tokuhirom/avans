package me.geso.avans;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.Param;
import me.geso.avans.trigger.ResponseFilter;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2WithBase;
import me.geso.servlettester.jetty.JettyServletTester;
import me.geso.webscrew.response.WebResponse;

import com.google.common.collect.ImmutableMap;

@RunWith(Enclosed.class)
public class ControllerBaseTest {

	// --------------------------------------------------------------

	@Ignore
	public static class MyController extends ControllerBase {
		@ResponseFilter
		public void filter(WebResponse repsonse) {
		}
	}

	@Test
	public void test() {
		try (final MyController controller = new MyController()) {
			final Filters filters = controller.getFilters();
			assertThat(filters.getResponseFilters().size(), is(1));
		}
	}

	// --------------------------------------------------------------

	static interface Mixin {
		@ResponseFilter
		public default void filter(WebResponse repsonse) {
		}
	}

	@Ignore
	public static abstract class Controller3 extends ControllerBase implements
			Mixin {
	}

	@Ignore
	public static class Controller2 extends Controller3 implements Mixin {
	}

	@Test
	public void test2() {
		// filter scanner should not add same filter twice.
		try (final Controller2 controller = new Controller2()) {
			final Filters filters = controller.getFilters();
			filters.getResponseFilters().forEach(it -> System.out.println(it));
			assertThat(filters.getResponseFilters().size(), is(1));
		}
	}

	// --------------------------------------------------------------

	static interface MixinA {
		@ResponseFilter
		public default void filterA(WebResponse repsonse) {
		}

		@ResponseFilter
		public default void filterA2(WebResponse repsonse) {
		}
	}

	static interface MixinB {
		@ResponseFilter
		public default void filterB(WebResponse repsonse) {
		}
	}

	@Ignore
	public static abstract class ControllerX extends ControllerBase implements
			MixinA {
		@Override
		@ResponseFilter
		public void filterA2(WebResponse repsonse) {
		}
	}

	@Ignore
	public static class ControllerY extends ControllerX implements MixinB {
		@Override
		@ResponseFilter
		public void filterA(WebResponse repsonse) {
		}
	}

	@Test
	public void test3() throws Exception {
		// filter scanner should not add same filter twice.
		try (final ControllerY controller = new ControllerY()) {
			final Filters filters = controller.getFilters();
			filters.getResponseFilters().forEach(it -> System.out.println(it));
			final String methods = filters.getResponseFilters().stream()
				.map(it -> it.getName()).collect(Collectors.joining(","));
			System.out.println(methods);
			assertThat(filters.getResponseFilters(), is(Arrays.asList(
				this.method(MixinA.class, "filterA"),
				this.method(MixinA.class, "filterA2"),
				this.method(ControllerX.class, "filterA2"),
				this.method(MixinB.class, "filterB"),
				this.method(ControllerY.class, "filterA")
				)));
		}
	}

	private Method method(Class<?> klass, String name) throws Exception {
		return klass.getMethod(name, WebResponse.class);
	}

	/**
	 * OptionalLong must be accept empty string as OptionalLong.empty().
	 */
	public static class TestOptionalLong {
		public static class Controller extends ControllerBase {
			@GET("/")
			public WebResponse root(@Param("q") OptionalLong q) {
				return this.renderText("q=" + q.orElse(5963));
			}
		}

		@Test
		public void testX() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			// without string parameter
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("q=5963", mech2.get("/").execute()
					.getResponseBodyAsString());
			});
			// with empty string
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("q=5963", mech2.get("/")
					.addQueryParameter("q", "")
					.execute()
					.getResponseBodyAsString());
			});
			// with string
			JettyServletTester.runServlet(
				servlet,
				baseURI -> {
					final Mech2WithBase mech2 = new Mech2WithBase(Mech2
						.builder()
						.build(), baseURI);
					assertEquals("q=4649", mech2.get("/")
						.addQueryParameter("q", "4649")
						.execute()
						.getResponseBodyAsString());
				});
		}
	}

	/**
	 * OptionalInt must be accept empty string as OptionalLong.empty().
	 */
	public static class TestOptionalInt {
		public static class Controller extends ControllerBase {
			@GET("/")
			public WebResponse root(@Param("q") OptionalInt q) {
				return this.renderText("q=" + q.orElse(5963));
			}
		}

		@Test
		public void testX() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			// without string parameter
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("q=5963", mech2.get("/").execute()
					.getResponseBodyAsString());
			});
			// with empty string
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("q=5963", mech2.get("/")
					.addQueryParameter("q", "")
					.execute()
					.getResponseBodyAsString());
			});
			// with string
			JettyServletTester.runServlet(
				servlet,
				baseURI -> {
					final Mech2WithBase mech2 = new Mech2WithBase(Mech2
						.builder()
						.build(), baseURI);
					assertEquals("q=4649", mech2.get("/")
						.addQueryParameter("q", "4649")
						.execute()
						.getResponseBodyAsString());
				});
		}
	}

	/**
	 * OptionalDouble must be accept empty string as OptionalLong.empty().
	 */
	public static class TestOptionalDouble {
		public static class Controller extends ControllerBase {
			@GET("/")
			public WebResponse root(@Param("q") OptionalDouble q) {
				return this.renderText("q=" + q.orElse(3.14));
			}
		}

		@Test
		public void testX() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			// without string parameter
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("q=3.14", mech2.get("/").execute()
					.getResponseBodyAsString());
			});
			// with empty string
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("q=3.14", mech2.get("/")
					.addQueryParameter("q", "")
					.execute()
					.getResponseBodyAsString());
			});
			// with string
			JettyServletTester.runServlet(
				servlet,
				baseURI -> {
					final Mech2WithBase mech2 = new Mech2WithBase(Mech2
						.builder()
						.build(), baseURI);
					assertEquals("q=2.71", mech2.get("/")
						.addQueryParameter("q", "2.71")
						.execute()
						.getResponseBodyAsString());
				});
		}
	}

	/**
	 * OptionalLong must be accept empty string as OptionalLong.empty().
	 */
	public static class TestOptionalStringParameter {
		public static class Controller extends ControllerBase {
			@GET("/")
			public WebResponse root(@Param("q") Optional<String> q) {
				return this.renderText("q=" + q.orElse("Missing"));
			}
		}

		@Test
		public void testX() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("q=Missing", mech2.get("/").execute()
					.getResponseBodyAsString());
			});
			// with empty string
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("q=Missing", mech2.get("/")
					.addQueryParameter("q", "")
					.execute()
					.getResponseBodyAsString());
			});
			JettyServletTester.runServlet(
				servlet,
				baseURI -> {
					final Mech2WithBase mech2 = new Mech2WithBase(Mech2
						.builder()
						.build(), baseURI);
					assertEquals("q=4649", mech2.get("/")
						.addQueryParameter("q", "4649")
						.execute()
						.getResponseBodyAsString());
				});
		}
	}

	public static class TestRedirect {
		public static class Controller extends ControllerBase {
			@GET("/")
			public WebResponse root() {
				return this.redirect("/xxx/yyy?foo=bar");
			}

			@GET("/xxx/yyy")
			public WebResponse yyy() {
				return this.renderText("OK");
			}
		}

		@Test
		public void testX() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				assertEquals("OK", mech2.get("/").execute()
					.getResponseBodyAsString());
			});
		}
	}

	public static class TestRedirectWithQueryParameters {
		public static class Controller extends ControllerBase {
			@GET("/a")
			public WebResponse a() throws IOException, URISyntaxException {
				return this.redirect("/ok", ImmutableMap.<String, String>builder()
					.put("a", "1")
					.build());
			}

			@GET("/b")
			public WebResponse b() throws Exception {
				return this.redirect("/ok", ImmutableMap.<String, String>builder()
					.put("a", "1")
					.put("b", "2")
					.build());
			}

			@GET("/c")
			public WebResponse c() throws Exception {
				return this.redirect("/ok", ImmutableMap.<String, String>builder()
					.put("a", "1")
					.put("b", "2")
					.put("c", "3")
					.build());
			}

			@GET("/z")
			public WebResponse z() throws IOException, URISyntaxException {
				return this.redirect("/ok", ImmutableMap.<String, String>builder()
					.build());
			}
		}

		@Test
		public void testX() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				mech2.disableRedirectHandling();

				assertEquals("/ok", getPathQuery(mech2, "/z"));
				// Only one parameter
				assertEquals("/ok?a=1", getPathQuery(mech2, "/a"));
				// There is two parameters.
				assertEquals("/ok?a=1&b=2", getPathQuery(mech2, "/b"));
				// There is three parameters.
				assertEquals("/ok?a=1&b=2&c=3", getPathQuery(mech2, "/c"));
			});
		}

		private String getPathQuery(Mech2WithBase mech2, String path) throws URISyntaxException, IOException {
			String location = mech2.get(path).execute()
				.getResponse()
				.getFirstHeader("Location")
				.getValue();
			// Remove scheme, host and port.
			Pattern pattern = Pattern.compile("^http://[^/]+");
			return pattern.matcher(location).replaceFirst("");
		}
	}

	public static class TestGetCurrentURL {
		public static class Controller extends ControllerBase {
			@GET("/a")
			public WebResponse a() throws IOException, URISyntaxException {
				return this.renderText(this.getCurrentURL().toString());
			}
		}

		@Test
		public void testX() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				mech2.disableRedirectHandling();

				HttpResponse response = mech2.get("/a")
					.addQueryParameter("x", "ok")
					.execute()
					.getResponse();
				String got = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				System.out.println(got);
				assertTrue(got.matches("http://127.0.0.1:[0-9]+/a\\?x=ok"));
			});
		}
	}

	public static class TestURIFor {
		public static class Controller extends ControllerBase {
			@GET("/uriFor1")
			public WebResponse a() throws IOException, URISyntaxException {
				return this.renderText(this.uriFor("/o").toString());
			}

			@GET("/b")
			public WebResponse b() throws IOException, URISyntaxException {
				return this.renderText(this.uriFor("/x", ImmutableMap.<String, String>builder()
					.put("foo", "bar")
					.build()).toString());
			}
		}

		@Test
		public void testURIFor() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				mech2.disableRedirectHandling();

				{
					HttpResponse response = mech2.get("/uriFor1")
						.addQueryParameter("x", "ok")
						.execute()
						.getResponse();
					String got = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					System.out.println(got);
					assertTrue(got.matches("http://127.0.0.1:[0-9]+/o"));
				}
				{
					HttpResponse response = mech2.get("/b")
						.addQueryParameter("x", "ok")
						.execute()
						.getResponse();
					String got = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					System.out.println(got);
					assertTrue(got.matches("http://127.0.0.1:[0-9]+/x\\?foo=bar"));
				}
			});
		}
	}

	public static class TestURIWith {
		public static class Controller extends ControllerBase {
			@GET("/b")
			public WebResponse b() throws IOException, URISyntaxException {
				return this.renderText(this.uriWith(ImmutableMap.<String, String>builder()
					.put("foo", "bar")
					.build()).toString());
			}
		}

		@Test
		public void testURIWith() throws Exception {
			final AvansServlet servlet = new AvansServlet();
			servlet.registerClass(Controller.class);
			JettyServletTester.runServlet(servlet, baseURI -> {
				final Mech2WithBase mech2 = new Mech2WithBase(Mech2.builder()
					.build(), baseURI);
				mech2.disableRedirectHandling();

				{
					HttpResponse response = mech2.get("/b")
						.addQueryParameter("x", "ok")
						.execute()
						.getResponse();
					String got = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					System.out.println(got);
					assertTrue(got.matches("http://127.0.0.1:[0-9]+/b\\?x=ok&foo=bar"));
				}
			});
		}
	}

}
