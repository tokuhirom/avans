package me.geso.webscrew.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.geso.avans.AvansUtil;
import me.geso.webscrew.Parameters;
import me.geso.webscrew.request.impl.WebRequestUploadImpl;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents HTTP request. The object isn't thread safe. You
 * shouldn't share this object between threads.
 * 
 * @author tokuhirom
 *
 */
public class WebRequest {
	private final HttpServletRequest servletRequest;
	private MultiMap<String, WebRequestUpload> uploads;
	private Parameters queryParams;
	private Parameters bodyParams;

	public WebRequest(final HttpServletRequest request) {
		this.servletRequest = request;
	}

	/**
	 * Get PATH_INFO.
	 * 
	 * @return
	 */
	public String getPathInfo() {
		return this.servletRequest.getPathInfo();
	}

	/**
	 * Get header string.
	 * 
	 * @param name
	 * @return
	 */
	public String getHeader(String name) {
		return this.servletRequest.getHeader(name);
	}

	/**
	 * Get all header values by name.
	 * 
	 * @param name
	 * @return
	 */
	public List<String> getHeaders(String name) {
		return Collections.list(this.servletRequest.getHeaders(name));
	}

	/**
	 * Get all headers in Map.
	 * 
	 * @return
	 */
	public Map<String, List<String>> getHeaderMap() {
		Map<String, List<String>> map = new TreeMap<>();
		Enumeration<String> headerNames = this.servletRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			final String name = headerNames.nextElement();
			final ArrayList<String> values = Collections.list(this.servletRequest
					.getHeaders(name));
			map.put(name, values);
		}
		return map;
	}

	/**
	 * Get CONTENT_LENGTH.
	 * 
	 * @return
	 */
	public int getContentLength() {
		return this.servletRequest.getContentLength();
	}

	/**
	 * Get HTTP_METHOD
	 * 
	 * @return
	 */
	public String getMethod() {
		return this.servletRequest.getMethod();
	}

	/**
	 * Returns the part of this request's URL from the protocol name up to the
	 * query string in the first line of the HTTP request. The web container
	 * does not decode this String. For example:
	 * 
	 * <table summary="Examples of Returned Values">
	 * <tr align=left>
	 * <th>First line of HTTP request</th>
	 * <th>Returned Value</th>
	 * <tr>
	 * <td>POST /some/path.html HTTP/1.1
	 * <td>
	 * <td>/some/path.html
	 * <tr>
	 * <td>GET http://foo.bar/a.html HTTP/1.0
	 * <td>
	 * <td>/a.html
	 * <tr>
	 * <td>HEAD /xyz?a=b HTTP/1.1
	 * <td>
	 * <td>/xyz
	 * </table>
	 *
	 * <p>
	 *
	 * @return a <code>String</code> containing the part of the URL from the
	 *         protocol name up to the query string
	 */
	public String getRequestURI() {
		return this.servletRequest.getRequestURI();
	}

	/**
	 * Get QUERY_STRING.
	 * 
	 * @return
	 */
	public String getQueryString() {
		return this.servletRequest.getQueryString();
	}

	/**
	 * Get cooies.
	 * 
	 * @return
	 */
	public Cookie[] getCookies() {
		return this.servletRequest.getCookies();
	}

	/**
	 * Get session object.
	 * 
	 * @return
	 */
	public HttpSession getSession() {
		return this.servletRequest.getSession();
	}

	/**
	 * Change session id.
	 */
	public void changeSessionId() {
		this.servletRequest.changeSessionId();
	}

	/**
	 * Read JSON from content-body. And parse it. This method runs hibernate
	 * validator. If the validation was failed, it throws runtime exception.
	 * 
	 * @param typeReference
	 * @return
	 */
	@SneakyThrows
	public <T> T readJSON(@NonNull final TypeReference<T> typeReference) {
		ServletInputStream inputStream = this.servletRequest.getInputStream();

		ObjectMapper mapper = new ObjectMapper();
		T instance = mapper.readValue(inputStream, typeReference);
		if (instance != null) {
			return instance;
		} else {
			throw new RuntimeException("null found... in content body");
		}
	}

	/**
	 * Read JSON from content-body. And parse it. This method runs hibernate
	 * validator. If the validation was failed, it throws runtime exception.
	 * 
	 * @param klass
	 * @return
	 */
	@SneakyThrows
	public <T> T readJSON(@NonNull final Class<T> klass) {
		ServletInputStream inputStream = this.servletRequest.getInputStream();

		ObjectMapper mapper = new ObjectMapper();
		T instance = mapper.readValue(inputStream, klass);
		if (instance != null) {
			return instance;
		} else {
			throw new RuntimeException("null found... in content body");
		}
	}

	protected String getCharacterEncoding() {
		return this.servletRequest.getCharacterEncoding();
	}

	/**
	 * Get uploaded file object by name.
	 * 
	 * @param name
	 * @return
	 */
	public Optional<WebRequestUpload> getFileItem(String name) {
		@SuppressWarnings("unchecked")
		Collection<WebRequestUpload> items = (Collection<WebRequestUpload>) this.getFileItemMap().get(name);
		if (items == null) {
			return Optional.empty();
		}
		return items.stream().findFirst();
	}

	/**
	 * Get uploaded file items by name.
	 * 
	 * @param name
	 * @return
	 */
	public Collection<WebRequestUpload> getFileItems(String name) {
		@SuppressWarnings("unchecked")
		Collection<WebRequestUpload> items = (Collection<WebRequestUpload>) this.getFileItemMap().get(name);
		if (items == null) {
			return new ArrayList<>();
		}
		return items;
	}

	/**
	 * Get all uploaded file items.
	 * 
	 * @return
	 */
	public MultiMap<String, WebRequestUpload> getFileItemMap() {
		this.getBodyParams(); // initialize this.uploads
		return this.uploads;
	}

	/**
	 * Create new ServletFileUpload instance. You can override this method.
	 *
	 * <p>
	 * See also commons-fileupload.
	 * </p>
	 * 
	 * @return
	 */
	protected ServletFileUpload createServletFileUpload() {
		FileItemFactory fileItemFactory = new DiskFileItemFactory();
		return new ServletFileUpload(fileItemFactory);
	}

	public Parameters getQueryParams() {
		if (this.queryParams == null) {
			this.queryParams = AvansUtil.parseQueryString(
					this.getQueryString(), this.getCharacterEncoding());
		}
		return queryParams;
	}

	@SneakyThrows
	public Parameters getBodyParams() {
		if (this.bodyParams == null) {
			if (this.servletRequest.getContentType().startsWith(
					"application/x-www-form-urlencoded")) {
				String queryString = IOUtils.toString(this.servletRequest.getInputStream(), this.getCharacterEncoding());
				this.bodyParams = AvansUtil.parseQueryString(
						queryString, this.getCharacterEncoding());
			} else if (ServletFileUpload.isMultipartContent(servletRequest)) {
				MultiMap<String,String> bodyParams = new MultiValueMap<String, String>();
				MultiMap<String,WebRequestUpload> uploads = new MultiValueMap<>();
				ServletFileUpload servletFileUpload = this
						.createServletFileUpload();
				List<FileItem> fileItems = servletFileUpload
						.parseRequest(this.servletRequest);
				for (FileItem fileItem : fileItems) {
					if (fileItem.isFormField()) {
						String value = fileItem.getString(this
								.getCharacterEncoding());
						bodyParams.put(fileItem.getFieldName(), value);
					} else {
						uploads.put(fileItem.getFieldName(), new WebRequestUploadImpl(fileItem));
					}
				}
				this.uploads = uploads;
				this.bodyParams = new Parameters(bodyParams);
			}
		}
		return this.bodyParams;
	}

	@SneakyThrows
	public void setCharacterEncoding(String env) {
		this.servletRequest.setCharacterEncoding(env);
	}

}
