package me.geso.sample.view;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NonNull;
import me.geso.sample.controller.BaseController;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.WebResponse;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FreemarkerView {
    private final String templatePath;
    private final Map<String, Object> dataModel;
	private final Configuration configuration;
    private final BaseController controller;
    private final Charset charset;
    private final String contentType;

    public FreemarkerView(@NonNull Configuration configuration, @NonNull String templatePath, @NonNull BaseController controller) {
		this.configuration = configuration;
        this.templatePath = templatePath;
        this.controller = controller;
		this.dataModel = new HashMap<>();
		this.dataModel.put("helper", new Helper(controller));
        this.charset = StandardCharsets.UTF_8;
        this.contentType = "text/html; charset=utf-8";
	}

	public FreemarkerView param(String key, Object value) {
		this.dataModel.put(key, value);
		return this;
	}

	public WebResponse render() throws IOException, TemplateException {
		final Template template = this.configuration
				.getTemplate(templatePath);
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
