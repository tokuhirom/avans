package me.geso.avans.mustache;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import lombok.NonNull;
import me.geso.avans.HTMLFilterProvider;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.WebResponse;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Build {@code MustacheFactory} instance.<br>
 * <br>
 * Example:<br>
 * {@code
 * final String fileBase = this.getBaseDirectory().resolve("src/main/resources/templates").toString();
 * return new FallbackMustacheFactory("templates", new File(fileBase));
	 * } <br>
 * You should cache this instance.
 * 
 */
public class MustacheView {
	private final MustacheFactory mustacheFactory;

	public MustacheView(MustacheFactory mustacheFactory) {
		this.mustacheFactory = mustacheFactory;
	}
	
	/**
	 * Create a response object by mustache template engine.
	 *
	 * @param template
	 * @param context
	 * @return
	 */
	public WebResponse render(@NonNull HTMLFilterProvider controller,
			@NonNull String template,
			Object context) {
		final Mustache mustache = mustacheFactory.compile(template);
		final StringWriter writer = new StringWriter();
		mustache.execute(writer, context);
		String bodyString = writer.toString();
		bodyString = controller.filterHTML(bodyString);

		final byte[] body = bodyString.getBytes(StandardCharsets.UTF_8);

		final ByteArrayResponse res = new ByteArrayResponse(200, body);
		res.setContentType("text/html; charset=utf-8");
		res.setContentLength(body.length);
		return res;
	}

}
