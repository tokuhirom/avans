package me.geso.avans;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.geso.avans.annotation.BeanParam;
import me.geso.avans.annotation.JsonParam;
import me.geso.avans.annotation.Param;
import me.geso.avans.annotation.PathParam;
import me.geso.avans.annotation.UploadFile;
import me.geso.avans.jackson.JacksonJsonParamReader;
import me.geso.avans.jackson.JacksonJsonView;
import me.geso.avans.trigger.ParamProcessor;
import me.geso.avans.trigger.ResponseConverter;
import me.geso.webscrew.HttpServletRequestUtils;
import me.geso.webscrew.response.ByteArrayResponse;
import me.geso.webscrew.response.RedirectResponse;
import me.geso.webscrew.response.WebResponse;

/**
 * You should create this object per HTTP request.
 *
 * @author tokuhirom
 */
@Slf4j
public abstract class ControllerBase implements Controller,
		JacksonJsonView, HTMLFilterProvider, JSONErrorPageRenderer,
		ValidatorProvider, TextRendererProvider, JacksonJsonParamReader {
	private static final Logger LOGGER = LoggerFactory
		.getLogger(ControllerBase.class);
	private static final Logger EXCEPTION_ROOT_CAUSE_LOGGER = LoggerFactory
		.getLogger("avans.exception.RootCause");
	private static final Logger EXCEPTION_STACK_TRACE_LOGGER = LoggerFactory
		.getLogger("avans.exception.StackTrace");
	private final ConcurrentHashMap<Class<?>, Filters> filters = new ConcurrentHashMap<>();
	private final Map<String, Object> pluginStash = new HashMap<>();
	private HttpServletResponse servletResponse;
	private HttpServletRequest servletRequest;
	private Map<String, String> pathParams;

	@Override
	public void init(final HttpServletRequest servletRequest,
			final HttpServletResponse servletResponse,
			final Map<String, String> captured) {
		this.servletResponse = servletResponse;
		this.servletRequest = servletRequest;
		this.setDefaultCharacterEncoding();

		this.pathParams = Collections.unmodifiableMap(captured);
	}

	private void setDefaultCharacterEncoding() {
		this.servletResponse.setCharacterEncoding("UTF-8");
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return this.servletRequest;
	}

	/**
	 * Create new redirect response. You can use relative url here.
	 *
	 * @param location destination URL... relative path is ok.
	 * @return Created response object.
	 */
	public RedirectResponse redirect(@NonNull final String location) {
		return new RedirectResponse(location);
	}

	/**
	 * Create new redirect response. You can use relative url here.
	 *
	 * @param location destination URL... relative path is ok.
	 * @return Created response object
	 */
	public RedirectResponse redirect(@NonNull final String location, @NonNull Map<String, String> parameters)
			throws URISyntaxException {
		final URIBuilder uriBuilder = new URIBuilder(location);
		parameters.entrySet().stream().forEach(
			e -> uriBuilder.setParameter(e.getKey(), e.getValue())
			);
		return new RedirectResponse(uriBuilder.build().toString());
	}

	/**
	 * Create new "405 Method Not Allowed" response in JSON.
	 *
	 * @return Created response object
	 */
	public WebResponse errorMethodNotAllowed() {
		return this.renderError(405, "Method Not Allowed");
	}

	/**
	 * Create new "403 Forbidden" response in JSON.
	 *
	 * @return Created response object
	 */
	public WebResponse errorForbidden() {
		return this.errorForbidden("Forbidden");
	}

	public WebResponse errorForbidden(final String message) {
		return this.renderError(403, message);
	}

	/**
	 * Create new "404 Not Found" response in JSON.
	 *
	 * @return
	 */
	public WebResponse errorNotFound() {
		return this.renderError(404, "Not Found");
	}

	/**
	 * Create new text/plain response.
	 *
	 * @param text
	 * @return
	 */
	@Override
	public WebResponse renderText(final String text) {
		if (text == null) {
			throw new IllegalArgumentException("text must not be null");
		}
		final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

		final ByteArrayResponse res = new ByteArrayResponse(200, bytes);
		res.setContentType("text/plain; charset=utf-8");
		return res;
	}

	@Override
	public String filterHTML(final String html) {
		String h = html;
		for (final Method filter : this.getFilters().getHtmlFilters()) {
			try {
				h = (String)filter.invoke(this, h);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return h;
	}

	@Override
	public void invoke(final Method method,
			final HttpServletRequest servletRequest,
			final HttpServletResponse servletResponse,
			final Map<String, String> captured) {
		try {
			this.init(servletRequest, servletResponse, captured);

			final WebResponse response = this.makeResponse(this, method);
			for (final Method filter : this.getFilters().getResponseFilters()) {
				filter.invoke(this, response);
			}
			response.write(servletResponse);
		} catch (final Throwable e) {
			final WebResponse response = this.handleException(e);
			try {
				response.write(servletResponse);
			} catch (final IOException ioe) {
				this.logException(ioe);
				throw new RuntimeException(ioe);
			}
		}
	}

	private void logException(Throwable e) {
		@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
		final Throwable root = this.unwrapRuntimeException(e);
		// Logging root cause in the log.
		{
			final StackTraceElement[] stackTrace = root.getStackTrace();
			if (stackTrace.length > 0) {
				final StackTraceElement ste = stackTrace[0];
				EXCEPTION_ROOT_CAUSE_LOGGER.error(
					"{}, {}, {}, {}, {}: {} at {}.{}({}:{})",
					this.servletRequest.getMethod(),
					this.servletRequest.getPathInfo(),
					this.servletRequest.getHeader("User-Agent"),
					this.servletRequest.getRemoteAddr(),
					root.getClass(),
					//
					root.getMessage(),
					ste.getClassName(),
					ste.getMethodName(),
					ste.getFileName(),
					ste.getLineNumber()
					);
			} else {
				EXCEPTION_ROOT_CAUSE_LOGGER.error("{}, {}, {}, {}, {}: {}",
					this.servletRequest.getMethod(),
					this.servletRequest.getPathInfo(),
					this.servletRequest.getHeader("User-Agent"),
					this.servletRequest.getRemoteAddr(),
					root.getClass(),
					//
					root.getMessage()
					);
			}
		}
		// Logging all messages in the fat log.
		{
			final StringWriter writer = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			final String s = writer.toString();

			EXCEPTION_STACK_TRACE_LOGGER.error("{}: {}\n{}", root.getClass(),
				root.getMessage(), s);
		}
	}

	// You can override me.
	public WebResponse handleException(Throwable e) {
		this.logException(e);
		return this.renderError(500, "Internal Server Error");
	}

	private Throwable unwrapRuntimeException(Throwable e) {
		while ((e instanceof RuntimeException || e instanceof InvocationTargetException)
			&& e.getCause() != null) {
			e = e.getCause();
		}
		return e;
	}

	Filters getFilters() {
		return this.filters
			.computeIfAbsent(
                    this.getClass(),
                    (klass) -> {
                        final FilterScanner scanner = new FilterScanner();
                        scanner.scan(klass);
                        return scanner.build();
                    });
    }

	private WebResponse makeResponse(final Controller controller,
			final Method method) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException,
			ServletException, InstantiationException {
		for (final Method filter : this.getFilters()
			.getBeforeDispatchTriggers()) {
			try {
				@SuppressWarnings("unchecked")
				final Optional<WebResponse> webResponse = (Optional<WebResponse>)filter
					.invoke(this);
				if (webResponse == null) {
					throw new NullPointerException(
						"@BeforeDispatchTrigger shouldn't returned null. It should return `Optional<WebResponse>`.");
				}
				if (webResponse.isPresent()) {
					return webResponse.get();
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		final Parameter[] parameters = method.getParameters();
		final Object[] params = new Object[parameters.length];
		final List<String> missingParameters = new ArrayList<>();
		final List<String> illegalParameters = new ArrayList<>();
		for (int i = 0; i < parameters.length; ++i) {
			final Parameter parameter = parameters[i];
			if (parameter.getAnnotation(BeanParam.class) != null) {
				// Process parameters annotated by @BeanParam
				final List<Field> declaredFields = getAllDeclaredFields(parameter.getType());
				final Object bean = parameter.getType().newInstance();
				for (final Field field : declaredFields) {
					if (Modifier.isStatic(field.getModifiers())) {
						// skip static field.
						continue;
					}
					final ParameterProcessorResult value = this.getParameterValue(
						field, field.getType(), field.getGenericType(), field.getName());
					if (value.hasResponse()) {
						return value.getResponse();
					} else if (value.hasData()) {
						field.setAccessible(true);
						field.set(bean, value.getData());
					} else if (value.hasIllegalParameter()) {
						illegalParameters.add(value.getIllegalParameter());
					} else {
						missingParameters.add(value.getMissingParameter());
					}
				}
				params[i] = bean;
			} else {
				final ParameterProcessorResult value = this
                        .getParameterValue(parameter, parameter.getType(), parameter.getParameterizedType(),
                                           parameter.getName());
                if (value.hasResponse()) {
                    return value.getResponse();
                } else if (value.hasData()) {
                    params[i] = value.getData();
                } else if (value.hasIllegalParameter()) {
                    illegalParameters.add(value.getIllegalParameter());
                } else {
                    missingParameters.add(value.getMissingParameter());
                }
            }
        }
        if (!illegalParameters.isEmpty()) {
            return this.errorIllegalParameters(illegalParameters);
        }
        if (!missingParameters.isEmpty()) {
            return this.errorMissingMandatoryParameters(missingParameters);
        }
        final Optional<WebResponse> validationResult = this
                .validateParameters(method, params);
        if (validationResult.isPresent()) {
            return validationResult.get();
        }

		Object res;
		try {
			res = method.invoke(controller, params);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// It caused by programming error.
			LOGGER.error("{}: {}: {}, {}", e, this.getServletRequest()
				.getPathInfo(),
				controller, params);
			throw new RuntimeException(e);
		}
		if (res instanceof WebResponse) {
			return (WebResponse)res;
		} else if (res == null) {
			throw new RuntimeException(
				"dispatch method must not return NULL");
		} else {
			for (final Method converter : this.getFilters()
				.getResponseConverters()) {
				final ResponseConverter annotation = converter
					.getAnnotation(ResponseConverter.class);
				if (res.getClass().isAssignableFrom(annotation.value())) {
					// Signature is : Optional<WebResponse> r(T o);
					final Object v = converter.invoke(this, res);
					if (v == null) {
						throw new NullPointerException(
							"@ResponseConverter must not return NULL");
					} else if (v instanceof Optional) {
						final Optional<?> ov = (Optional<?>)v;
						if (ov.isPresent()) {
							return (WebResponse)ov.get();
						} else {
							// Call next response converter.
							continue;
						}
					} else {
						throw new RuntimeException(
							"@ResponseConverter must return Optional<WebResponse>");
					}
				}
			}
			throw new RuntimeException(String.format(
				"Unknown return value from action: %s(%s)", res.getClass(),
				this.servletRequest.getPathInfo()));
		}
	}

	private WebResponse errorIllegalParameters(List<String> badNumberFormatParameters) {
		final StringBuilder buf = new StringBuilder();
		buf.append("Illegal parameter: ");
		buf.append(badNumberFormatParameters.stream().collect(Collectors.joining(", ")));
		return this.renderError(HttpServletResponse.SC_BAD_REQUEST, new String(buf));
	}

	private List<Field> getAllDeclaredFields(final Class<?> type) {
		final List<Field> fields = new ArrayList<>();
		Class<?> klass = type;
		while (klass != null && !klass.equals(Object.class)) {
			Collections.addAll(fields, klass.getDeclaredFields());
			klass = klass.getSuperclass();
		}
		return fields;
	}

	protected WebResponse errorMissingMandatoryParameters(
			List<String> missingParameters) {
		final StringBuilder buf = new StringBuilder();
		buf.append("Missing mandatory parameter: ");
		buf.append(missingParameters.stream().collect(Collectors.joining(", ")));
		return this.renderError(HttpServletResponse.SC_BAD_REQUEST, new String(buf));
	}

	private <T> ParameterProcessorResult getParameterValue(
			final AnnotatedElement parameter, final Class<?> type, Type parameterizedType, final String parameterName)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, ServletException
	{
		// @ParamProcessor
		// public ParamProcessorResult paramUpperQ(Parameter parameter);
		for (final Method pp : this.getFilters().getParamProcessors()) {
			final ParamProcessor paramProcessor = pp
				.getAnnotation(ParamProcessor.class);

			if (paramProcessor.targetClass().isAssignableFrom(
				type)) {
				if (paramProcessor.targetAnnotation() == ParamProcessor.class
					|| parameter.getAnnotation(paramProcessor
						.targetAnnotation()) != null) {
					final Object result = pp.invoke(this, parameter);
					if (result == null) {
						throw new NullPointerException(
							"@ParamProcessor returns null: "
								+ pp);
					} else if (result instanceof ParameterProcessorResult) {
						if (((ParameterProcessorResult)result).hasData()
							|| ((ParameterProcessorResult)result)
								.hasResponse()) {
							return (ParameterProcessorResult)result;
						}
					} else {
						throw new RuntimeException(
							"@ParamProcessor should return ParameterProcessorResult, but "
								+ pp);
					}
				}
			}
		}

		final Annotation[] annotations = parameter.getAnnotations();
		for (final Annotation annotation : annotations) {
			if (annotation instanceof JsonParam) {
				final InputStream is = this.servletRequest.getInputStream();
				final Object value = this.readJsonParam(is, type);
				return ParameterProcessorResult.fromData(value);
			} else if (annotation instanceof Param) {
				final String name = ((Param)annotation).value();
				final String value = this.getServletRequest()
					.getParameter(name);
				try {
					return this.getObjectFromParameterObjectValue(name,
																  type, parameterizedType, value);
				} catch (NumberFormatException ignored) {
					return ParameterProcessorResult.illegalParameter(name);
				}
			} else if (annotation instanceof PathParam) {
				final String name = ((PathParam)annotation).value();
				final String value = this.pathParams.get(name);
				try {
					return this.getObjectFromParameterObjectValue(name,
																  type, parameterizedType, value);
				} catch (NumberFormatException ignored) {
					return ParameterProcessorResult.illegalParameter(name);
				}
			} else if (annotation instanceof UploadFile) {
				// @UploadFile
				final String name = ((UploadFile)annotation).value();
				if (type == Part.class) {
					final Part part = this.servletRequest.getPart(name);
					if (part != null) {
						return ParameterProcessorResult.fromData(part);
					} else {
						return ParameterProcessorResult.missingParameter(name);
					}
				} else if (type == Part[].class) {
					final Part[] parts = this.servletRequest.getParts()
						.stream().filter(part -> name.equals(part.getName()))
						.toArray(Part[]::new);
					return ParameterProcessorResult.fromData(parts);
				} else if (type == Optional.class) {
					// It must be `Optional<Part>`
					try {
						final Part part = this.servletRequest.getPart(name);
						if (part != null) {
							return ParameterProcessorResult
								.fromData(
                                        Optional.of(part));
                        } else {
                            return ParameterProcessorResult.fromData(Optional
                                                                             .empty());
                        }
                    } catch (final IOException e) {
                        // We must catch this exception.
                        // Since jetty throws exception if the request doesn't
                        // have a part.
                        // See
                        // org.eclipse.jetty.util.MultiPartInputStreamParser.parse.
                        log.info("{}: {}", e.getClass(), e.getMessage());
                        return ParameterProcessorResult.fromData(Optional
                                                                         .empty());
                    }
                } else {
                    throw new RuntimeException(
                            String.format(
                                    "You shouldn't use @UploadFile annotation with %s. You must use Part or Part[]",
                                    type));
                }
            }
        }

		// Programming error. You may forget to specify the annotation.
		throw new RuntimeException(String.format(
			"There is no way to create parameter: %s, %s, %s",
			this.getClass().getName(), this.getServletRequest()
				.getPathInfo(),
			parameterName));
	}

	private ParameterProcessorResult getObjectFromParameterObjectValue(
			final String name,
			final Class<?> type,
			final Type parameterizedType,
			final String value) {
		if (type.equals(String.class)) {
			if (value != null) {
				return ParameterProcessorResult.fromData(value);
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(int.class) || type.equals(Integer.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(Integer
					.parseInt(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(long.class) || type.equals(Long.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult
					.fromData(Long.parseLong(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(short.class) || type.equals(Short.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult
					.fromData(Short.parseShort(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(double.class) || type.equals(Double.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(Double
					.parseDouble(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(
						Boolean.parseBoolean(value));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(BigInteger.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(new BigInteger(value, 10));
			} else {
				return ParameterProcessorResult.missingParameter(name);
			}
		} else if (type.equals(OptionalInt.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(OptionalInt.of(Integer
					.parseInt(value)));
			} else {
				return ParameterProcessorResult.fromData(OptionalInt.empty());
			}
		} else if (type.equals(OptionalLong.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(OptionalLong.of(Long
					.parseLong(value)));
			} else {
				return ParameterProcessorResult.fromData(OptionalLong.empty());
			}
		} else if (type.equals(OptionalDouble.class)) {
			if (value != null && !value.isEmpty()) {
				return ParameterProcessorResult.fromData(OptionalDouble
					.of(Double
						.parseDouble(value)));
			} else {
				return ParameterProcessorResult
					.fromData(OptionalDouble.empty());
			}
		} else if (type.equals(String[].class)) {
			String[] parameterValues = getServletRequest().getParameterValues(name);
			if (parameterValues == null) {
				parameterValues = new String[]{};
			}
			return ParameterProcessorResult
				.fromData(parameterValues);
		} else if (type.equals(Long[].class)) {
			final String[] parameterValues = getServletRequest().getParameterValues(name);
			if (parameterValues == null) {
				return ParameterProcessorResult
						.fromData(new Long[] { });
			} else {
				final Long[] values = Arrays.stream(parameterValues)
						.map(Long::parseLong)
						.toArray(Long[]::new);
				return ParameterProcessorResult
						.fromData(values);
			}
		} else if (type.equals(long[].class)) {
			final String[] parameterValues = getServletRequest().getParameterValues(name);
			if (parameterValues == null) {
				return ParameterProcessorResult
						.fromData(new long[] { });
			} else {
				final long[] values = Arrays.stream(parameterValues)
						.mapToLong(Long::parseLong)
						.toArray();
				return ParameterProcessorResult
						.fromData(values);
			}
		} else if (type.equals(Integer[].class)) {
			final String[] parameterValues = getServletRequest().getParameterValues(name);
			if (parameterValues == null) {
				return ParameterProcessorResult
						.fromData(new Integer[]{});
			} else {
				final Integer[] values = Arrays.stream(parameterValues)
						.map(Integer::parseInt)
						.toArray(Integer[]::new);
				return ParameterProcessorResult
						.fromData(values);
			}
		} else if (type.equals(int[].class)) {
			final String[] parameterValues = getServletRequest().getParameterValues(name);
			if (parameterValues == null) {
				return ParameterProcessorResult
						.fromData(new int[] { });
			} else {
				final int[] values = Arrays.stream(parameterValues)
						.mapToInt(Integer::parseInt)
						.toArray();
				return ParameterProcessorResult
						.fromData(values);
			}
		} else if (type.equals(Boolean[].class)) {
			final String[] parameterValues = getServletRequest().getParameterValues(name);
			if (parameterValues == null) {
				return ParameterProcessorResult
						.fromData(new Boolean[]{});
			} else {
				final Boolean[] values = Arrays.stream(parameterValues)
						.map(Boolean::parseBoolean)
						.toArray(Boolean[]::new);
				return ParameterProcessorResult
						.fromData(values);
			}
		} else if (type.equals(boolean[].class)) {
			final String[] parameterValues = getServletRequest().getParameterValues(name);
			if (parameterValues == null) {
				return ParameterProcessorResult
						.fromData(new boolean[]{});
			} else {
				final boolean[] values = new boolean[parameterValues.length];
				IntStream.range(0, parameterValues.length)
						.forEach(i -> {
							values[i] = Boolean.parseBoolean(parameterValues[i]);
						});
				return ParameterProcessorResult
						.fromData(values);
			}
		} else if (type.equals(BigInteger[].class)) {
			final String[] parameterValues = getServletRequest().getParameterValues(name);
			if (parameterValues == null) {
				return ParameterProcessorResult
						.fromData(new BigInteger[]{});
			} else {
				final BigInteger[] values = Arrays.stream(parameterValues)
						.map(v -> new BigInteger(v, 10))
						.toArray(BigInteger[]::new);
				return ParameterProcessorResult
						.fromData(values);
			}
		} else if (type.equals(List.class)) {
			if (parameterizedType instanceof ParameterizedType) {
				final Type[] actualTypeArguments = ((ParameterizedType)parameterizedType).getActualTypeArguments();
				if (actualTypeArguments != null && actualTypeArguments.length == 1) {
					final Type type1 = actualTypeArguments[0];
					if (type1 instanceof Class) {
						final String[] parameterValues = getServletRequest().getParameterValues(name);
						if (parameterValues == null || parameterValues.length == 0) {
							return ParameterProcessorResult
									.fromData(Collections.emptyList());
						}

						if (((Class)type1).isAssignableFrom(String.class)) {
							return ParameterProcessorResult
									.fromData(Collections.unmodifiableList(Arrays.asList(parameterValues)));
						} else if (((Class)type1).isAssignableFrom(Integer.class)) {
							return ParameterProcessorResult
									.fromData(
											Collections.unmodifiableList(
													Arrays.stream(parameterValues)
															.map(Integer::valueOf)
															.collect(Collectors.toList())));
						} else if (((Class)type1).isAssignableFrom(Long.class)) {
							return ParameterProcessorResult
									.fromData(
											Collections.unmodifiableList(
													Arrays.stream(parameterValues)
															.map(Long::valueOf)
															.collect(Collectors.toList())));
						} else if (((Class)type1).isAssignableFrom(Double.class)) {
							return ParameterProcessorResult
									.fromData(
											Collections.unmodifiableList(
													Arrays.stream(parameterValues)
															.map(Double::valueOf)
															.collect(Collectors.toList())));
						} else if (((Class)type1).isAssignableFrom(Boolean.class)) {
							return ParameterProcessorResult
									.fromData(
											Collections.unmodifiableList(
													Arrays.stream(parameterValues)
															.map(Boolean::valueOf)
															.collect(Collectors.toList())));
						} else if (((Class)type1).isAssignableFrom(BigInteger.class)) {
							return ParameterProcessorResult
									.fromData(
											Collections.unmodifiableList(
													Arrays.stream(parameterValues)
													.map(v -> new BigInteger(v, 10))
													.collect(Collectors.toList())));
						}
					}
				}
			}
			// Programming error
			throw new RuntimeException(String.format(
					"No valid type parameter for List<E>: '%s', '%s'. Valid types are: List<String>, List<Long>,"
					+ " List<Integer>, and List<Double>", parameterizedType, name));
        } else if (type.isAssignableFrom(LocalDateTime.class)) {
            if (value != null && !value.isEmpty()) {
                try {
                    return ParameterProcessorResult.fromData(LocalDateTime.parse(value));
                } catch (DateTimeParseException e) {
                    return ParameterProcessorResult.illegalParameter(name);
                }
            } else {
                return ParameterProcessorResult.missingParameter(name);
            }
        } else if (type.isAssignableFrom(LocalTime.class)) {
            if (value != null && !value.isEmpty()) {
                try {
                    return ParameterProcessorResult.fromData(LocalTime.parse(value));
                } catch (DateTimeParseException e) {
                    return ParameterProcessorResult.illegalParameter(name);
                }
            } else {
                return ParameterProcessorResult.missingParameter(name);
            }
        } else if (type.isAssignableFrom(LocalDate.class)) {
            if (value != null && !value.isEmpty()) {
                try {
                    return ParameterProcessorResult.fromData(LocalDate.parse(value));
                } catch (DateTimeParseException e) {
                    return ParameterProcessorResult.illegalParameter(name);
                }
            } else {
                return ParameterProcessorResult.missingParameter(name);
            }
        } else if (type.equals(Optional.class)) {
            // avans supports Optional<String> only.
            if (value != null && !value.isEmpty()) {
                if (parameterizedType instanceof ParameterizedType) {
                    final Type[] actualTypeArguments = ((ParameterizedType) parameterizedType).getActualTypeArguments();
                    if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                        final Type type1 = actualTypeArguments[0];
                        if (type1 instanceof Class) {
                            if (((Class) type1).isAssignableFrom(String.class)) {
                                return ParameterProcessorResult.fromData(Optional.of(value));
                            } else if (((Class) type1).isAssignableFrom(Boolean.class)) {
                                return ParameterProcessorResult.fromData(Optional.of(Boolean.parseBoolean(value)));
                            } else if (((Class) type1).isAssignableFrom(LocalDateTime.class)) {
                                try {
                                    return ParameterProcessorResult.fromData(Optional.of(LocalDateTime.parse(value)));
                                } catch (DateTimeParseException e) {
                                    return ParameterProcessorResult.illegalParameter(name);
                                }
                            } else if (((Class) type1).isAssignableFrom(LocalTime.class)) {
                                try {
                                    return ParameterProcessorResult.fromData(Optional.of(LocalTime.parse(value)));
                                } catch (DateTimeParseException e) {
                                    return ParameterProcessorResult.illegalParameter(name);
                                }
                            } else if (((Class) type1).isAssignableFrom(LocalDate.class)) {
                                try {
                                    return ParameterProcessorResult.fromData(Optional.of(LocalDate.parse(value)));
                                } catch (DateTimeParseException e) {
                                    return ParameterProcessorResult.illegalParameter(name);
                                }
                            } else if (((Class) type1).isAssignableFrom(Integer.class)) {
                                throw new RuntimeException(String.format(
                                        "%s: invalid type for '%s'(%s): Optional<Integer> is not supported. You " +
                                        "should use OptionalInt instead.",
                                        getServletRequest().getPathInfo(), name, parameterizedType));
                            }
                        }
                    }
                }

				// Programming error
				throw new RuntimeException(String.format(
						"Invalid type parameter for '%s': Valid type is 'Optional<String>' but you specified '%s'.",
						name, parameterizedType));
			} else {
				return ParameterProcessorResult.fromData(Optional.empty());
			}
		} else {
			// Programming error
			throw new RuntimeException(String.format(
				"Unknown parameter type '%s' for '%s'", type, name));
		}
	}

	@Override
	public void close() {
	}

	private String generatePluginStashKey(Class<?> pluginClass, String key) {
		return pluginClass.getName() + "#" + key;
	}

	@Override
	public Optional<Object> getPluginStashValue(Class<?> pluginClass, String key) {
		final Object object = this.pluginStash.get(this.generatePluginStashKey(
			pluginClass, key));
		return Optional.ofNullable(object);
	}

	@Override
	public void setPluginStashValue(Class<?> pluginClass, String key,
			Object value) {
		this.pluginStash.put(this.generatePluginStashKey(pluginClass, key),
			value);
	}

	@Override
	public Object computePluginStashValueIfAbsent(Class<?> pluginClass,
			String key,
			Supplier<?> supplier) {
		return this.pluginStash.computeIfAbsent(
			this.generatePluginStashKey(pluginClass, key),
			(fullKey) -> supplier.get());
	}

	@Override
	public Map<String, String> getPathParams() {
		return this.pathParams;
	}

	/**
	 * [EXPERIMENTAL] Get the URL for current HTTP request.
	 * Return value includes query string.
	 *
	 * @return Reconstructed URL
	 * @throws MalformedURLException
	 */
	public URL getCurrentURL() throws MalformedURLException {
		return HttpServletRequestUtils.getCurrentURL(getServletRequest());
	}

	/**
	 * [EXPERIMENTAL] Constructs an absolute URI object based on the application root, the provided path, and the additional arguments and query parameters provided.
	 * This method cares context path. You can use relative path from context root.
	 *
	 * For example, if your context path is {@code http://example.com/xxx/},
	 * <code>uriFor("/x")</code> returns {@code http://example.com/xxx/x}
	 *
	 * @param path Path from the current URL. You can use root relative URL from context root.
	 * @param parameters Query parameters.
	 * @return Constructed URI.
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public URI uriFor(String path, Map<String, String> parameters) throws URISyntaxException, MalformedURLException {
		return HttpServletRequestUtils.uriFor(getServletRequest(), path, parameters);
	}

	/**
	 * [EXPERIMENTAL] Short hand for {@code this.uriFor(path, Collections.emptyMap())}.
	 *
	 * @param path Path for destination.
	 * @return Constructed URL
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public URI uriFor(final String path) throws URISyntaxException, MalformedURLException {
		return HttpServletRequestUtils.uriFor(getServletRequest(), path, Collections.emptyMap());
	}

	/**
	 * [EXPERIMENTAL] Returns a rewritten URI object for the current request.
	 * Key/value pairs passed in will override existing parameters.
	 * You can remove an existing parameter by passing in an undef value.
	 * Unmodified pairs will be preserved.
	 *
	 * @param parameters Query parameters.
	 * @return Constructed URI.
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public URI uriWith(final Map<String, String> parameters) throws URISyntaxException, MalformedURLException {
		return HttpServletRequestUtils.uriWith(getServletRequest(), parameters);
	}

}
