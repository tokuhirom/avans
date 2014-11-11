package me.geso.avans;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import me.geso.avans.trigger.ResponseFilter;
import me.geso.webscrew.response.WebResponse;

import org.junit.Test;

public class ControllerBaseTest {

	// --------------------------------------------------------------

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

	public static abstract class Controller3 extends ControllerBase implements
			Mixin {
	}

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

	public static abstract class ControllerX extends ControllerBase implements
			MixinA {
		@Override
		@ResponseFilter
		public void filterA2(WebResponse repsonse) {
		}
	}

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

}
