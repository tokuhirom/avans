package me.geso.avans.trigger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation represents the method should call as response converter.
 * <p>
 * Response converter reduces your controller code. e.g. you can return the DTO
 * object from controller method, and convert it as JSON response in response
 * converter.
 * <p>
 * I don't recommend to use this.
 * 
 * <pre>
 * <code>public class MyController extends ControllerBase {
 *   &#064;ResponseConverter(APIResponse.class)
 *   public Optional&gt;WebResponse&lt; responseConverter(APIResponse o) {
 *     return Optional.of(this.renderJSON(o));
 *   }
 * }</code>
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResponseConverter {
	public Class<?> value();
}
