package <%= @package %>.controller;

import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.avans.mustache.MustacheView;
import me.geso.webscrew.response.WebResponse;

public class RootController extends ControllerBase
    implements MustacheView {
    @GET("/")
    public WebResponse index() {
        return this.renderMustache("index.mustache", null);
    }
}
