package <%= @package %>.controller;

import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.avans.mustache.MustacheViewMixin;
import me.geso.avans.mustache.MustacheView;
import me.geso.webscrew.response.WebResponse;

import java.io.File;

import com.github.mustachejava.DefaultMustacheFactory;

public class RootController extends ControllerBase
    implements MustacheViewMixin {
    @GET("/")
    public WebResponse index() {
        return this.renderMustache("index.mustache", null);
    }

	public MustacheView getMustacheView() {
		return new MustacheView(new DefaultMustacheFactory(new File("templates/")));
	}
}
