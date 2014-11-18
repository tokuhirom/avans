package me.geso.avans;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for Avans.
 */
public class AvansServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Dispatcher dispatcher = new Dispatcher();
	private static final Logger logger = LoggerFactory
			.getLogger(AvansServlet.class);

	/**
	 * You can register classes by web.xml. You can write web.xml as
	 * {@code <init-param><param-name>package</param-name><param-value>pkg1,pkg2</param-value></init-param>}
	 * or
	 * {@code <init-param><param-name>class</param-name><param-value>klass1,klass2</param-value></init-param>}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		final String pkg = servletConfig.getInitParameter("package");
		if (pkg != null) {
			for (final String p : pkg.split(",")) {
				logger.info("Registering package: {}", p);
				this.registerPackage(p);
			}
		}

		/**
		 * If you are using inner static class, please
		 * 
		 * @see <a
		 *      href="http://stackoverflow.com/questions/7007831/instantiate-nested-static-class-using-class-forname">here</a>
		 */
		final String classCsv = servletConfig.getInitParameter("class");
		if (classCsv != null) {
			for (final String className : classCsv.split(",")) {
				logger.info("Registering class: {}", className);
				try {
					final Class<?> klass = Class.forName(className);
					this.registerClass((Class<? extends Controller>) klass);
				} catch (final ClassNotFoundException e) {
					logger.error("AvansServlet can't load class: {}", className);
					throw new RuntimeException(e);
				}
			}
		}
	}

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
