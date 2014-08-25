package com.example.kitchen.controller;

import lombok.Data;
import me.geso.avans.AvansAPIResponse;
import me.geso.avans.AvansResponse;

import com.example.kitchen.KitchenWebApplication;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RootController {
	public static AvansResponse root(KitchenWebApplication web) {
		// This code render tmpl/root.mustache.

		TmplParams params = new TmplParams();
		params.setName("太郎");
		return web.renderMustache("root.mustache", params);
	}
	
	@Data
	public static class TmplParams {
		String name;
	}

	public static AvansResponse json(KitchenWebApplication web) {
		return web.renderJSON(new AvansAPIResponse<>(new MyObject("John")));
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
