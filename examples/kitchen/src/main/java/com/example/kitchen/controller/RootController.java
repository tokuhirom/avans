package com.example.kitchen.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import me.geso.avans.APIResponse;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.QueryParam;
import me.geso.webscrew.response.WebResponse;

public class RootController extends ControllerBase {
	@GET("/")
	public WebResponse root() {
		// This code render tmpl/root.mustache.

		TmplParams params = new TmplParams();
		params.setName("太郎");
		return this.renderMustache("root.mustache", params);
	}

	@GET("/q/q/query")
	public APIResponse<MyObject> qqquery(@QueryParam("hoge") String hoge,
										 @BodyParam("foo") int foo) {
		return new APIResponse<>(new MyObject("John"));
	}

	@Data
	public static class TmplParams {
		String name;
	}

	@GET("/json")
	public WebResponse json() {
		return this.renderJSON(new APIResponse<>(new MyObject("John")));
	}


	@Data
	public static class MyObject {
		private String name;

		@JsonCreator
		public MyObject(@JsonProperty("name") String name) {
			this.name = name;
		}
	}
}
