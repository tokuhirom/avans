package ${package}.controller;

import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.avans.mustache.MustacheViewMixin;
import me.geso.avans.mustache.MustacheView;

import com.github.mustachejava.DefaultMustacheFactory;

public abstract class BaseController extends ControllerBase
    implements MustacheViewMixin {

	public MustacheView getMustacheView() {
		return new MustacheView(new DefaultMustacheFactory("templates/"));
	}
}

