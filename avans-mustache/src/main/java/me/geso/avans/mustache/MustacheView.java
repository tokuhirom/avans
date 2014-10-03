package me.geso.avans.mustache;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;

import lombok.NonNull;
import me.geso.avans.Controller;
import me.geso.avans.HTMLFilterProvider;
import me.geso.webscrew.response.ByteArrayResponse;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

public interface MustacheView extends Controller, HTMLFilterProvider {

	/**
	 * Create a response object by mustache template engine.
	 *
	 * @param template
	 * @param context
	 * @return
	 */
	public default ByteArrayResponse renderMustache(@NonNull String template,
			Object context) {
		final Path tmplDir = this.getMustacheTemplateDirectory();
		final DefaultMustacheFactory factory = new DefaultMustacheFactory(
				tmplDir.toFile());
		final Mustache mustache = factory.compile(template);
		final StringWriter writer = new StringWriter();
		mustache.execute(writer, context);
		String bodyString = writer.toString();
		bodyString = this.filterHTML(bodyString);

		final byte[] body = bodyString.getBytes(Charset.forName("UTF-8"));

		final ByteArrayResponse res = new ByteArrayResponse(200, body);
		res.setContentType("text/html; charset=utf-8");
		res.setContentLength(body.length);
		return res;
	}

	public default Path getMustacheTemplateDirectory() {
		return this.getBaseDirectory().resolve("templates");
	}
}
