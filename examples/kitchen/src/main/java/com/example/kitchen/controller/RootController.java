package com.example.kitchen.controller;

import lombok.Data;
import me.geso.avans.APIResponse;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.QueryParam;
import me.geso.avans.mustache.MustacheView;
import me.geso.avans.mustache.MustacheViewMixin;
import me.geso.webscrew.response.WebResponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mustachejava.DefaultMustacheFactory;

public class RootController extends ControllerBase implements MustacheViewMixin {
	@GET("/")
	public WebResponse root() {
		// This code render tmpl/root.mustache.

		final TmplParams params = new TmplParams();
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

	@Override
	public MustacheView getMustacheView() {
		return new MustacheView(new DefaultMustacheFactory("templates"));
	}

	@Override
	public String filterHTML(String bodyString) {
		// TODO Auto-generated method stub
		return null;
	}

}
