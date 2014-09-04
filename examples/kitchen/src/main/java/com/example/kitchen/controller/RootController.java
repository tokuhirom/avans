package com.example.kitchen.controller;

import lombok.Data;
import me.geso.avans.APIResponse;
import me.geso.avans.ControllerBase;
import me.geso.avans.WebResponse;
import me.geso.avans.annotation.GET;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RootController extends ControllerBase {
	@GET("/")
	public WebResponse root() {
		// This code render tmpl/root.mustache.

		TmplParams params = new TmplParams();
		params.setName("太郎");
		return this.renderMustache("root.mustache", params);
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
