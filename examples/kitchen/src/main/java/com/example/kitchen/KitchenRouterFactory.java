package com.example.kitchen;

import com.example.kitchen.controller.RootController;

import me.geso.avans.AvansResponse;
import me.geso.routes.WebRouter;

public class KitchenRouterFactory {
	public static WebRouter<Action> build() {
		WebRouter<Action> router = new WebRouter<>();
		router.get("/", RootController::root);
		router.get("/json", RootController::json);
		return router;
	}

	@FunctionalInterface
	public static interface Action {
		public AvansResponse run(KitchenWebApplication web);
	}
}
