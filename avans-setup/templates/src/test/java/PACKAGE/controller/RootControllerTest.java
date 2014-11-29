package [% package %].controller;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RootControllerTest {
    private Mech2WithBase mech;
    private Tomcat tomcat;

    @Before
    public void before() throws ServletException, LifecycleException, URISyntaxException {
        this.tomcat = new Tomcat();
        tomcat.setPort(0);
        org.apache.catalina.Context webContext = tomcat.addWebapp("/", new File("src/main/webapp").getAbsolutePath());
        webContext.getServletContext().setAttribute(Globals.ALT_DD_ATTR, "src/main/webapp/WEB-INF/web.xml");
        tomcat.start();

        int port = tomcat.getConnector().getLocalPort();
        String url = "http://127.0.0.1:" + port;
        this.mech = new Mech2WithBase(Mech2.builder().build(), new URI(url));
    }

    @After
    public void after() throws Exception {
        if (this.tomcat != null) {
            this.tomcat.stop();
        }
    }

    @Test
    public void testRoot() throws IOException, URISyntaxException {
        final Mech2Result result = mech.get("/").execute();
        assertEquals(200, result.getResponse().getStatusLine().getStatusCode());
        assertTrue(result.getResponseBodyAsString().contains("Hello"));
    }
}
