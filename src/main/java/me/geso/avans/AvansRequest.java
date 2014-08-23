package me.geso.avans;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import lombok.SneakyThrows;

import com.fasterxml.jackson.databind.ObjectMapper;

// TODO port all methods from Plack::Request
/**
 * Not thread safe.
 * 
 * @author tokuhirom
 *
 */
public class AvansRequest {
	private HttpServletRequest request;
	private Map<String, List<FileItem>> uploads;
	private Map<String, String[]> parameters;

	public AvansRequest(HttpServletRequest request) {
		try {
			request.setCharacterEncoding(this.getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		this.request = request;
	}

	public String getPathInfo() {
		return this.request.getPathInfo();
	}

	public String getHeader(String name) {
		return this.request.getHeader(name);
	}

	public List<String> getHeaders(String name) {
		return Collections.list(this.request.getHeaders(name));
	}

	public int getContentLength() {
		return this.request.getContentLength();
	}

	public String getMethod() {
		return this.request.getMethod();
	}

	public String getRequestURI() {
		return this.request.getRequestURI();
	}

	public String getQueryString() {
		return this.request.getQueryString();
	}

	public Cookie[] getCookies() {
		return this.request.getCookies();
	}

	public HttpSession getSession() {
		return this.request.getSession();
	}

	public void changeSessionId() {
		this.request.changeSessionId();
	}

	/**
	 * Read JSON from content-body. And parse it. This method runs hibernate
	 * validator. If the validation was failed, it throws runtime exception.
	 * 
	 * @param klass
	 * @return
	 */
	@SneakyThrows
	public <T> T readJSON(Class<T> klass) {
		ServletInputStream inputStream = this.request.getInputStream();

		ObjectMapper mapper = new ObjectMapper();
		T instance = mapper.readValue(inputStream, klass);
		if (instance != null) {
			this.validate(instance);
			return instance;
		} else {
			throw new RuntimeException("null found... in content body");
		}
	}

	public <T> void validate(T o) {
		ValidatorFactory validatorFactory = Validation
				.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<T>> violations = validator.validate(o);
		if (!violations.isEmpty()) {
			String message = violations.stream().map(e -> {
				return e.getPropertyPath() + " " + e.getMessage();
			}).collect(Collectors.joining(","));
			throw new AvansValidationException(message);
		}
	}

	/**
	 * Read parameters from query string/content-body
	 * 
	 * @param name
	 * @return
	 */
	public Optional<String> getParameter(String name) {
		String[] strings = this.getParameterMap().get(name);
		if (strings == null) {
			return Optional.empty();
		}
		if (strings.length == 0) {
			return Optional.empty();
		}
		return Optional.of(strings[0]);
	}

	public List<String> getParameters(String name) {
		String[] strings = this.getParameterMap().get(name);
		if (strings == null) {
			return new ArrayList<>();
		} else {
			return Arrays.asList(strings);
		}
	}

	protected String getCharacterEncoding() {
		return "UTF-8";
	}

	public OptionalInt getIntParam(String name) {
		Optional<String> parameter = this.getParameter(name);
		if (parameter.isPresent()) {
			return OptionalInt.of(Integer.parseInt(parameter.get()));
		} else {
			return OptionalInt.empty();
		}
	}

	// TODO This may work. but it's not tested.
	public Optional<FileItem> getFileItem(String name) {
		List<FileItem> items = this.getFileItemMap().get(name);
		if (items == null || items.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(items.get(0));
	}

	// TODO This may work. but it's not tested.
	public List<FileItem> getFileItems(String name) {
		List<FileItem> items = this.getFileItemMap().get(name);
		if (items == null) {
			return new ArrayList<>();
		}
		return items;
	}
	
	public Map<String, List<FileItem>> getFileItemMap() {
		this.getParameterMap(); // initialize this.uploads
		return this.uploads;
	}

	public Map<String, String[]> getParameterMap() {
		if (this.parameters == null) {
			try {
				this.parameters = new HashMap<>(request.getParameterMap());

				if (ServletFileUpload.isMultipartContent(request)) {
					FileItemFactory fileItemFactory = this
							.createFileItemFactory();
					ServletFileUpload servletFileUpload = new ServletFileUpload(
							fileItemFactory);
					List<FileItem> fileItems = servletFileUpload
							.parseRequest(this.request);
					this.uploads = new HashMap<>();
					for (FileItem fileItem : fileItems) {
						if (fileItem.isFormField()) {
							String value = fileItem.getString(this.getCharacterEncoding());
							String[] strings = new String[1];
							strings[0] = value;
							this.parameters.put(fileItem.getFieldName(), strings);
						} else {
							if (this.uploads.containsKey(fileItem
									.getFieldName())) {
								this.uploads.get(fileItem.getFieldName()).add(
										fileItem);
							} else {
								List<FileItem> list = new ArrayList<>();
								list.add(fileItem);
								this.uploads.put(fileItem.getFieldName(), list);
							}
						}
					}
				}
			} catch (FileUploadException | UnsupportedEncodingException e) {
				this.parameters = null;
				throw new RuntimeException(e);
			}
		}
		return this.parameters;
	}

	/**
	 * You can override this method.
	 * 
	 * @return
	 */
	public FileItemFactory createFileItemFactory() {
		return new DiskFileItemFactory();
	}

	public ServletFileUpload createServletFileUpload() {
		return new ServletFileUpload();
	}

	// TODO: support ServletFileUpload

}
