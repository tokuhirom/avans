#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.Part;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.UploadFile;
import me.geso.webscrew.response.WebResponse;

@Slf4j
public class UploadController extends BaseController {

	@GET("/upload")
	public WebResponse upload() throws IOException, TemplateException {
		return this.freemarker("upload.html.ftl")
			.param("name", "John<>")
			.render();
	}

	@POST("/upload")
	public WebResponse uploadPost(@UploadFile("target") Part file) throws IOException {
		log.info("FILE {}", file);
		try (InputStream inputStream = file.getInputStream()) {
			String value = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
			return this.renderText(value);
		}
	}
}
