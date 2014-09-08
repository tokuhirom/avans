package me.geso.avans.validator;

import java.util.Optional;

import me.geso.avans.Controller;
import me.geso.webscrew.response.WebResponse;

/**
 * Validator interface for {@code @JsonParam}.
 * 
 * @author tokuhirom
 *
 */
public interface JsonParamValidator {
	public <T> Optional<WebResponse> validate(Controller controller, T bean);
}
