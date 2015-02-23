package me.geso.sample.controller;

import me.geso.avans.ControllerBase;
import me.geso.avans.trigger.ResponseFilter;
import me.geso.sample.ConfigLoader;
import me.geso.sample.view.FreemarkerView;
import me.geso.sample.view.FreemarkerViewFactory;
import me.geso.webscrew.response.WebResponse;

public abstract class BaseController extends ControllerBase {
	private static FreemarkerViewFactory freemarkerViewFactory = new FreemarkerViewFactory();

	public static boolean isDevelopment() {
		return ConfigLoader.getConfig().isDevelopment();
	}

	public FreemarkerView freemarker(String templatePath) {
		return BaseController.freemarkerViewFactory.create(templatePath, this);
	}

	@ResponseFilter
	public void securityFilters(WebResponse resp) {
		// Reducing MIME type security risks
		// http://msdn.microsoft.com/en-us/library/ie/gg622941(v=vs.85).aspx
		resp.addHeader("X-Content-Type-Options", "nosniff");

		// Avoid click jacking attacks
		// (If you want to display this site in frames, remove this header)
		// https://developer.mozilla.org/en-US/docs/Web/HTTP/X-Frame-Options
		resp.addHeader("X-Frame-Options", "DENY");

		// MUST NOT be cached by a shared cache, such as proxy server.
		resp.addHeader("Cache-Control", "private");
	}
}

