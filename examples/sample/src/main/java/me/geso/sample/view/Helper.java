package me.geso.sample.view;

import lombok.NonNull;
import me.geso.sample.controller.BaseController;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

public class Helper {
    private final BaseController controller;

    public Helper(BaseController controller) {
        this.controller = controller;
    }

	public String uriFor(String path) throws MalformedURLException, URISyntaxException {
		return String.valueOf(this.controller.uriFor(path));
	}

	public String uriFor(@NonNull String path, @NonNull Map<String, String> params)
			throws MalformedURLException, URISyntaxException {
		return String.valueOf(this.controller.uriFor(path, params));
	}

	public String uriWith(@NonNull Map<String, String> params)
			throws MalformedURLException, URISyntaxException {
		return String.valueOf(this.controller.uriWith(params));
	}

	public String currentURL() throws MalformedURLException {
		return String.valueOf(this.controller.getCurrentURL());
	}
}
