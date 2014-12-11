package com.example.kitchen;

import java.io.File;

import org.apache.catalina.Globals;
import org.apache.catalina.startup.Tomcat;

public class Main {
	public static void main(String[] args) throws Exception {
		new Main().doMain();
	}

	private void doMain() throws Exception {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(0);
		org.apache.catalina.Context webContext = tomcat.addWebapp("/",
				new File("src/main/webapp").getAbsolutePath());
		webContext.getServletContext().setAttribute(Globals.ALT_DD_ATTR,
				"src/main/webapp/WEB-INF/web.xml");
		tomcat.start();
	}

}
