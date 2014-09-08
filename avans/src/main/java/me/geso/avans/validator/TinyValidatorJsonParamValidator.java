package me.geso.avans.validator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import me.geso.avans.APIResponse;
import me.geso.avans.Controller;
import me.geso.tinyvalidator.DefaultMessageGenerator;
import me.geso.tinyvalidator.MessageGenerator;
import me.geso.tinyvalidator.Validator;
import me.geso.tinyvalidator.Violation;
import me.geso.webscrew.response.WebResponse;

/**
 * {@code @JsonParam} validatior using tinyvalidator.
 * 
 * @author tokuhirom
 *
 */
public class TinyValidatorJsonParamValidator implements JsonParamValidator {
	private Validator validator = new Validator();
	private MessageGenerator messageGenerator = new DefaultMessageGenerator();
	private static int FORBIDDEN = 403;
	
	@Override
	public <T> Optional<WebResponse> validate(Controller controller, T bean) {
		List<Violation<T>> violations = validator.validate(bean);
		if (violations.isEmpty()) {
			return Optional.empty(); // no validator violations.
		}

		// There is violations. Return validation errors.
		List<String> messages = violations.stream().map(violation -> {
			return messageGenerator.generateMessage(violation);
		}).collect(Collectors.toList());
		APIResponse<Object> apiResponse = new APIResponse<>(FORBIDDEN, messages, null);
		WebResponse jsonResponse = controller.renderJSON(apiResponse);
		return Optional.of(jsonResponse);
	}

	public Validator getValidator() {
		return validator;
	}
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	public MessageGenerator getMessageGenerator() {
		return messageGenerator;
	}
	public void setMessageGenerator(MessageGenerator messageGenerator) {
		this.messageGenerator = messageGenerator;
	}

}
