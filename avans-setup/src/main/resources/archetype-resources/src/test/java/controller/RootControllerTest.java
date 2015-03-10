#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import me.geso.mech2.Mech2Result;

public class RootControllerTest extends ControllerTestBase {
	@Test
	public void testRoot() throws IOException, URISyntaxException {
		final Mech2Result result = mech().get("/").execute();
		assertEquals(200, result.getResponse().getStatusLine().getStatusCode());
		assertTrue(result.getResponseBodyAsString().contains("Hello"));
	}
}
