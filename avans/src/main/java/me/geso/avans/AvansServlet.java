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
	public void registerPackage(final String packageName) {
		this.dispatcher.registerPackage(packageName);
	}

	/**
	 * Scan package and register all classes.
	 * 
	 * @param pkg
	 */
	public void registerPackage(final Package pkg) {
		this.dispatcher.registerPackage(pkg.getName());
	}

	/**
	 * Register paths from class.
	 * 
	 * @param klass
	 */
	public void registerClass(final Class<? extends Controller> klass) {
		this.dispatcher.registerClass(klass);
	}

	/**
	 * Get dispatcher object.
	 * 
	 * @return dispatcher object.
	 */
	public Dispatcher getDispatcher() {
		return this.dispatcher;
	}

	/**
	 * Do service.
	 */
	@Override
	public void service(final ServletRequest req, final ServletResponse res)
			throws ServletException, IOException {
		this.dispatcher.handler(
				(HttpServletRequest) req,
				(HttpServletResponse) res);
	}

}
