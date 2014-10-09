package me.geso.avans;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import me.geso.avans.trigger.ResponseFilter;
import me.geso.webscrew.response.WebResponse;

import org.junit.Test;

public class ControllerBaseTest {

	public static class MyController extends ControllerBase {
		@ResponseFilter
		public void filter(WebResponse repsonse) {
		}
	}

	@Test
	public void test() {
		try (final MyController controller = new MyController()) {
			final ControllerBase.Filters filters = controller.getFilters();
			assertThat(filters.getResponseFilters().size(), is(1));
		}
	}

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
			final ControllerBase.Filters filters = controller.getFilters();
			filters.getResponseFilters().forEach(it -> System.out.println(it));
			assertThat(filters.getResponseFilters().size(), is(1));
		}
	}

}
