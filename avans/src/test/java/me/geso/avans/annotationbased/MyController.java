package me.geso.avans.annotationbased;

import me.geso.avans.APIResponse;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.webscrew.WebResponse;

public class MyController extends ControllerBase {

	@GET("/")
	public WebResponse root() {
		APIResponse<String> res = new APIResponse<>("hoge");
		return renderJSON(res);
	}

	@POST("/postForm")
	public WebResponse postForm(@BodyParam("name") String name) {
		String text = "(postform)name:" + name;
		return renderTEXT(text);
	}

}
