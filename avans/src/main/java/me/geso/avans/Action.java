package me.geso.avans;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Action class. This class invokes action.
 * 
 * @author tokuhirom
 *
 */
public interface Action {
	public void invoke(HttpServletRequest request, HttpServletResponse response, Map<String, String> captured);
}
