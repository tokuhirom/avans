package com.example.kitchen;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.avans.Dispatcher;

import com.example.kitchen.controller.RootController;

public class KitchenServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Dispatcher dispatcher = new Dispatcher();
	static {
		dispatcher.registerPackage(RootController.class.getPackage().getName());
	}

	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatcher.handler(req, resp);
	}
}
