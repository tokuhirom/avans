package me.geso.avans.tinyvalidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.geso.avans.BasicAPIResponse;
import me.geso.avans.JSONRendererProvider;
import me.geso.avans.ValidatorProvider;
import me.geso.avans.annotation.BeanParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.tinyvalidator.ConstraintViolation;
import me.geso.tinyvalidator.Validator;
import me.geso.webscrew.response.WebResponse;

public interface TinyValidatorValidator extends ValidatorProvider,
		JSONRendererProvider {

	/**
	 * Validate parameters. If there is violations, return the WebResponse
	 * contains error messages.
	 */
	@Override
	public default Optional<WebResponse> validateParameters(Method method,
			Object[] values) {
		final Validator validator = new Validator();
		final Parameter[] parameters = method.getParameters();
		final List<String> violationMessages = new ArrayList<>();
		for (int i = 0; i < parameters.length; ++i) {
			final Parameter parameter = parameters[i];
			final Object value = values[i];
			final Annotation[] annotations = parameter.getAnnotations();
			for (final Annotation annotation : annotations) {
				if (annotation instanceof JsonParam || annotation instanceof BeanParam) {
					final List<ConstraintViolation> validate = validator
							.validate(value);
					validate.stream().forEach(
							violation -> {
								final String message = violation.getName()
										+ " "
										+ violation.getMessage();
								violationMessages.add(message);
							}
							);
				} else {
					final Optional<ConstraintViolation> constraintViolationOptional = validator
							.validateByAnnotation(annotation,
									parameter.getName(),
									value);
					if (constraintViolationOptional.isPresent()) {
						final ConstraintViolation constraintViolation = constraintViolationOptional
								.get();
						violationMessages.add(constraintViolation.getName()
								+ " "
								+ constraintViolation.getMessage());
					}
				}
			}
		}
		if (violationMessages.isEmpty()) {
			// There is no violations.
			return Optional.empty();
		} else {
			return Optional.of(this
					.createValidationFailedResponse(violationMessages));
		}
	}

	/**
	 * Create WebResponse from violation messages.
	 *
	 * @param violationMessages violation messages from tinyvalidator.
	 * @return new response object.
	 */
	public default WebResponse createValidationFailedResponse(
			List<String> violationMessages) {
		final Object body = new BasicAPIResponse(403, violationMessages);
		return this.renderJSON(200, body);
	}
}
