package me.geso.avans.annotationbased;

import me.geso.avans.AvansAPIResponse;
import me.geso.avans.AvansResponse;
import me.geso.avans.AvansWebApplication;

public class MyController {

	@GET("/")
	public static AvansResponse root(AvansWebApplication web) {
		AvansAPIResponse<String> res = new AvansAPIResponse<>("hoge");
		return web.renderJSON(res);
	}

	@POST("/postForm")
	public static AvansResponse postForm(AvansWebApplication web) {
		String text = "(postform)name:"
				+ web.getRequest().getParameter("name").get();
		return web.renderTEXT(text);
	}

}
