#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2WithBase;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.servlet.ServletException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class ControllerTestBase {
    private static Tomcat tomcat;
    private static Mech2WithBase mech;

    @BeforeClass
    public static void before() throws ServletException, LifecycleException, URISyntaxException {
        ControllerTestBase.tomcat = new Tomcat();
        tomcat.setPort(0);
        org.apache.catalina.Context webContext = tomcat.addWebapp("/", new File("src/main/webapp").getAbsolutePath());
        webContext.getServletContext().setAttribute(Globals.ALT_DD_ATTR, "src/main/webapp/WEB-INF/web.xml");
        tomcat.start();

        int port = tomcat.getConnector().getLocalPort();
        String url = "http://127.0.0.1:" + port;
        ControllerTestBase.mech = new Mech2WithBase(Mech2.builder().build(), new URI(url));
    }

    @AfterClass
    public static void after() throws ServletException, LifecycleException, URISyntaxException {
        ControllerTestBase.tomcat.stop();
    }

    public Mech2WithBase getMech() {
        return ControllerTestBase.mech;
    }

    public Tomcat getTomcat() {
        return ControllerTestBase.tomcat;
    }
}
