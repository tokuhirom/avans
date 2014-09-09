package me.geso.avans.methodparameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import me.geso.avans.Controller;
import me.geso.avans.annotation.BodyParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.QueryParam;
import me.geso.avans.annotation.UploadFile;
import me.geso.webscrew.Parameters;
import me.geso.webscrew.request.WebRequestUpload;

/***
 * Basic action form.
 *
 * @author tokuhirom
 *
 */
public class DefaultMethodParameterBuilder implements MethodParameterBuilder {
	private final List<String> validationErrors = new ArrayList<>();
	private final List<Object> parameters = new ArrayList<>();

	public Param[] build(Controller controller, Method method) {
		Parameter[] parameters = method.getParameters();
		Class<?>[] types = method.getParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Param[] params = new Param[parameters.length];
		for (int i = 0; i < parameters.length; ++i) {
			params[i] = makeParameter(controller, method, parameters[i],
					types[i],
					parameterAnnotations[i]);
		}
		return params;
	}

	private Param makeParameter(Controller controller, Method method,
			Parameter parameter,
			Class<?> type, Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof JsonParam) {
				Object param = controller.getRequest().readJSON(type);
				return new Param("json", param, annotations);
			} else if (annotation instanceof QueryParam) {
				String name = ((QueryParam) annotation).value();
				return new Param(name, getObjectFromParameter(annotation, name, type,
						controller.getRequest()
								.getQueryParams()), annotations);
			} else if (annotation instanceof BodyParam) {
				String name = ((BodyParam) annotation).value();
				return new Param(name, getObjectFromParameter(annotation, name, type,
						controller.getRequest()
								.getBodyParams()), annotations);
			} else if (annotation instanceof PathParam) {
				String name = ((PathParam) annotation).value();
				return new Param(name, getObjectFromParameter(annotation, name, type,
						controller.getPathParameters()), annotations);
			} else if (annotation instanceof UploadFile) {
				// @UploadFile
				String name = ((UploadFile) annotation).value();
				if (type == WebRequestUpload.class) {
					Optional<WebRequestUpload> maybeFileItem = controller
							.getRequest()
							.getFileItem(name);
					if (maybeFileItem.isPresent()) {
						return new Param(name, maybeFileItem.get(), annotations);
					} else {
						throw new RuntimeException(String.format(
								"Missing mandatory file: %s", name));
					}
				} else if (type == WebRequestUpload[].class) {
					WebRequestUpload[] items = controller.getRequest()
							.getFileItems(name)
							.toArray(new WebRequestUpload[0]);
					return new Param(name, items, annotations);
				} else if (type == Optional.class) {
					// It must be Optional<FileItem>
					Optional<WebRequestUpload> maybeFileItem = controller
							.getRequest()
							.getFileItem(name);
					return new Param(name, maybeFileItem, annotations);
				} else {
					throw new RuntimeException(
							String.format(
									"You shouldn't use @UploadFile annotation with %s. You must use FileItem or FileItem[]",
									type));
				}
			}
		}

		Optional<Param> param = this.MAKE_PARAMETER(controller, method, parameter);
		if (param.isPresent()) {
			return param.get();
		} else {
			throw new RuntimeException(String.format(
					"There is no way to create parameter: %s, %s, %s",
					controller.getClass().getName(), method.getName(),
					parameter.getName()));
		}
	}

	/**
	 * Hook point for generating parameters.
	 * 
	 * @param controller
	 * 
	 * @param method
	 * @param parameter
	 * @return
	 */
	protected Optional<Param> MAKE_PARAMETER(Controller controller, Method method,
			Parameter parameter) {
		// I AM HOOK POINT
		return null;
	}

	/**
	 * You can override this method for extending type converter.
	 * 
	 * @param annotation
	 * @param name
	 * @param type
	 * @param params
	 * @return
	 */
	protected Object getObjectFromParameter(Annotation annotation, String name,
			Class<?> type,
			Parameters params) {
		if (type.equals(String.class)) {
			if (!params.containsKey(name)) {
				throw new RuntimeException(String.format(
						"Missing mandatory parameter '%s' by %s", name,
						annotation.getClass().getName()));
			}
			return params.get(name);
		} else if (type.equals(int.class)) {
			if (!params.containsKey(name)) {
				throw new RuntimeException(String.format(
						"Missing mandatory parameter '%s' by %s", name,
						annotation.getClass().getName()));
			}
			return params.getInt(name);
		} else if (type.equals(long.class)) {
			if (!params.containsKey(name)) {
				throw new RuntimeException(String.format(
						"Missing mandatory parameter '%s' by %s", name,
						annotation.getClass().getName()));
			}
			return params.getLong(name);
		} else if (type.equals(double.class)) {
			if (!params.containsKey(name)) {
				throw new RuntimeException(String.format(
						"Missing mandatory parameter '%s' by %s", name,
						annotation.getClass().getName()));
			}
			return params.getDouble(name);
		} else if (type.equals(OptionalInt.class)) {
			return params.getOptionalInt(name);
		} else if (type.equals(OptionalLong.class)) {
			return params.getOptionalLong(name);
		} else if (type.equals(OptionalDouble.class)) {
			return params.getOptionalDouble(name);
		} else if (type.equals(Optional.class)) {
			// avans supports Optional<String> only.
			return params.getOptional(name);
		} else {
			throw new RuntimeException(String.format(
					"Unknown parameter type '%s' for '%s'", type, name));
		}
	}

	public Object[] getParameters() {
		return this.parameters.stream().toArray(Object[]::new);
	}

	public boolean hasValidationError() {
		return !this.validationErrors.isEmpty();
	}
}
