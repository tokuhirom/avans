package me.geso.avans.annotationbased;

import me.geso.avans.AvansWebApplicationTest.StringAPIResponse;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.webscrew.response.WebResponse;

@SuppressWarnings("deprecation")
public class MyController extends ControllerBase {

	@GET("/")
	public WebResponse root() {
		final StringAPIResponse res = new StringAPIResponse("hoge");
		return this.renderJSON(res);
	}

	@POST("/postForm")
	public WebResponse postForm(@BodyParam("name") final String name) {
		final String text = "(postform)name:" + name;
		return this.renderText(text);
	}

}
