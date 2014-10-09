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

}
