package ${package}.controller;

import me.geso.avans.annotation.GET;
import me.geso.webscrew.response.WebResponse;

public class RootController extends BaseController {
    @GET("/")
    public WebResponse index() {
        return this.renderMustache("index.mustache", null);
    }
}
