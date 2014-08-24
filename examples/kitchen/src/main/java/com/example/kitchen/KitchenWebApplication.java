package com.example.kitchen;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.avans.AvansResponse;
import me.geso.avans.AvansWebApplication;
import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;

import com.example.kitchen.KitchenRouterFactory.Action;

public class KitchenWebApplication extends AvansWebApplication {
	private static final WebRouter<KitchenRouterFactory.Action> router = KitchenRouterFactory
			.build();

	public KitchenWebApplication(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws IOException {
		super(servletRequest, servletResponse);
	}

	public void close() throws IOException {
	}

	public AvansResponse dispatch() {
		RoutingResult<Action> match = router.match(getRequest().getMethod(), getRequest().getPathInfo());
		if (match == null) {
			return this.errorNotFound();
		}
		if (!match.methodAllowed()) {
			return this.errorMethodNotAllowed();
		}
		this.setArgs(match.getCaptured());
		Action action = match.getDestination();
		AvansResponse response = action.run(this);
		return response;
	}

}
