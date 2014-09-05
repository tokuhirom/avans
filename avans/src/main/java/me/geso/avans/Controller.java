package me.geso.avans;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.geso.avans.webcomponents.Parameters;
import me.geso.avans.webcomponents.WebRequest;
import me.geso.avans.webcomponents.WebResponse;

public interface Controller {

	void init(HttpServletRequest request, HttpServletResponse response,
			Map<String, String> captured);

	public Optional<WebResponse> BEFORE_DISPATCH();
	public void AFTER_DISPATCH(WebResponse res);

	public WebRequest getRequest();

	Parameters getPathParameters();

	public WebResponse renderJSON(Object obj);

}
