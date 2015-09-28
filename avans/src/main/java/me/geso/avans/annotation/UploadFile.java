package me.geso.avans.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code public WebResponse do(@UploadFile("name") Part upload) ... }
 * or
 * {@code public WebResponse do(@UploadFile("name") Optional<Part> upload) ... }
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface UploadFile {
	String value();
}
