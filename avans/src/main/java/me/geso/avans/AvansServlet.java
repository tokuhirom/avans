package me.geso.avans;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AvansServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Dispatcher dispatcher = new Dispatcher();

	public void registerPackage(String packageName) {
		this.dispatcher.registerPackage(packageName);
	}

	public void registerPackage(Package pkg) {
		this.dispatcher.registerPackage(pkg.getName());
	}

	public void registerClass(Class<? extends Controller> klass) {
		this.dispatcher.registerClass(klass);
	}
	
	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		dispatcher.handler(
				(HttpServletRequest) req,
				(HttpServletResponse) res);
	}

}
