package me.geso.sample.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
import me.geso.avans.annotation.UploadFile;
import me.geso.webscrew.response.WebResponse;

@Slf4j
public class RootController extends BaseController {
    @GET("/")
    public WebResponse index(@Param("name") Optional<String> name) throws IOException, TemplateException {
        return this.freemarker("index.html.ftl")
				.param("name", name.orElse("Anonymous"))
				.render();
    }

	@GET("/upload")
	public WebResponse upload() throws IOException, TemplateException {
		return this.freemarker("upload.html.ftl")
				.param("name", "John<>")
				.render();
	}

	@POST("/upload")
	public WebResponse uploadPost(@UploadFile("target") Part file) throws IOException, TemplateException, ServletException {
		log.info("FILE {}", file);
		final List<Part> parts = new ArrayList<>(this.getServletRequest().getParts());
		if (parts.isEmpty()) {
			log.info("There is no uploads: {}", this.getServletRequest().getHeader("Content-Type"));
			return this.renderText("No uploads");
		} else {
			final Part part = parts.get(0);
			InputStream inputStream = part.getInputStream();
			String value = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
			return this.renderText(value);
		}
	}
}
