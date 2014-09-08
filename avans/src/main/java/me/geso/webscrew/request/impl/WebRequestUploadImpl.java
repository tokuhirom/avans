package me.geso.webscrew.request.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import me.geso.webscrew.request.WebRequestUpload;

import org.apache.commons.fileupload.FileItem;

public class WebRequestUploadImpl implements WebRequestUpload {

	private final FileItem fileItem;

	public WebRequestUploadImpl(FileItem fileItem) {
		this.fileItem = fileItem;
	}
	
	@Override
	public String getString(String encoding) {
		try {
			return this.fileItem.getString(encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getInputStream() {
		try {
			return this.fileItem.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getName() {
		return this.fileItem.getName();
	}

}
