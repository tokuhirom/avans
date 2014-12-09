package me.geso.avans.annotationbased;

import me.geso.avans.AvansWebApplicationTest.StringAPIResponse;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.annotation.Param;
import me.geso.webscrew.response.WebResponse;

public class MyController extends ControllerBase {

	@GET("/")
	public WebResponse root() {
		final StringAPIResponse res = new StringAPIResponse("hoge");
		return this.renderJSON(res);
	}

	@POST("/postForm")
	public WebResponse postForm(@Param("name") final String name) {
		final String text = "(postform)name:" + name;
		return this.renderText(text);
	}

}
