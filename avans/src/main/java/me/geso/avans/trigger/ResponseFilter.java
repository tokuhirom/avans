package me.geso.avans.trigger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation represents the method should call as response filter.
 * 
 * <pre>
 * <code>public class MyController extends ControllerBase {
 *   &#064;ResponseFilter
 *   public void beforeDispatch(WebResponse resp) {
 *     resp.addHeader("x-content-type-options", "nosniff");
 *   }
 * }</code>
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResponseFilter {
}
