package me.geso.avans.mustache;

import lombok.NonNull;
import me.geso.avans.Controller;
import me.geso.avans.HTMLFilterProvider;
import me.geso.webscrew.response.WebResponse;

/**
 * Mustache view for Avans web application framework.
 * 
 * History:<br>
 * At 0.27.2, I removed getMustacheTemplateDirectory method.<br>
 * 
 * @return
 */
public interface MustacheViewMixin extends Controller, HTMLFilterProvider {

	/**
	 * Create a response object by mustache template engine.
	 *
	 * @param template
	 * @param context
	 * @return
	 */
	public default WebResponse renderMustache(@NonNull String template,
			Object context) {
		MustacheView view = this.getMustacheView();
		return view.render(this, template, context);
	}

	/**
	 * Get mustache view.
	 * @return
	 */
	public MustacheView getMustacheView();
}
