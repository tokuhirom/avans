#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import freemarker.template.TemplateException;
import me.geso.avans.annotation.GET;
import me.geso.webscrew.response.WebResponse;

import java.io.IOException;

public class RootController extends BaseController {
    @GET("/")
    public WebResponse index() throws IOException, TemplateException {
        return this.freemarker("index.html.ftl")
				.param("name", "John<>")
				.render();
    }
}
