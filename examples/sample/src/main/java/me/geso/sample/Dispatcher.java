package me.geso.sample;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import me.geso.avans.Controller;
import me.geso.tinyorm.TinyORM;

@Slf4j
public class Dispatcher extends me.geso.avans.Dispatcher {
	private final Injector injector;

	public Dispatcher(final Injector injector) {
		this.injector = injector;
	}

	@Override
	public void runController(
			final Class<? extends Controller> controllerClass,
			final Method method, final HttpServletRequest request,
			final HttpServletResponse response,
			final Map<String, String> captured) {

		// start session
		// TODO If you want to use @SessionScoped, enable this.
		// request.getSession();

		Connection connection = null;

		try (Controller controller = injector.getInstance(controllerClass)) {
			connection = injector.getInstance(Connection.class);
			controller.invoke(method, request, response, captured);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			log.debug("Close JDBC Connection");

			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.error("JDBC Connection close error!", e);
			}
		}
	}
}
