package me.geso.avans.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import lombok.NonNull;
import me.geso.avans.HTMLFilterProvider;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.WebResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerView {
	private final Configuration configuration;
	private final Charset charset;
	private final String contentType;

	public FreeMarkerView(Configuration configuration, Charset charset,
			String contentType) {
		this.configuration = configuration;
		this.charset = charset;
		this.contentType = contentType;
	}

	public FreeMarkerView(Configuration configuration) {
		this(configuration, StandardCharsets.UTF_8, "text/html; charset=utf-8");
	}

	public WebResponse render(
			HTMLFilterProvider controller,
			@NonNull String templateName,
			Object dataModel) throws IOException, TemplateException {
		final Template template = this.configuration
				.getTemplate(templateName);
		final StringWriter writer = new StringWriter();
		template.process(dataModel, writer);

		final String bodyString = writer.toString();
		final String filteredString = controller.filterHTML(bodyString);
		final byte[] body = filteredString.getBytes(this.charset);

		final ByteArrayResponse res = new ByteArrayResponse(200, body);
		res.setContentType(this.contentType);
		res.setContentLength(body.length);
		return res;
	}
}
