package me.geso.avans;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for Avans.
 */
public class AvansServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Dispatcher dispatcher = new Dispatcher();

	/**
	 * Scan package and register all classes.
	 * 
	 * @param packageName
	 */
	public void registerPackage(String packageName) {
		this.dispatcher.registerPackage(packageName);
	}

	/**
	 * Scan package and register all classes.
	 * 
	 * @param packageName
	 */
	public void registerPackage(Package pkg) {
		this.dispatcher.registerPackage(pkg.getName());
	}

	/**
	 * Register paths from class.
	 * 
	 * @param packageName
	 */
	public void registerClass(Class<? extends Controller> klass) {
		this.dispatcher.registerClass(klass);
	}

	/**
	 * Get dispatcher object.
	 * 
	 * @return dispatcher object.
	 */
	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * Do service.
	 */
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		dispatcher.handler(
				(HttpServletRequest) req,
				(HttpServletResponse) res);
	}

}
