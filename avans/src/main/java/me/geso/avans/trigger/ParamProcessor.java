package me.geso.avans.trigger;

import java.lang.annotation.Annotation;
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
 * 
 * <pre>
 * public class MyController extends ControllerBase {
 *   {@code @ParamProcessor(targetClass=Member.class)}
 *   public {@code ParamProcessorData} paramProcessor(APIResponse o) {
 *     {@code Optional<Member>} member = this.getDB().getMemberFromToken(this.getRequest().getFirstHeader("X-Foo"));
 *     if (member.isPresent()) {
 *       {@code ParamProcessorData<Member>}
 *     } else {
 *     }
 *     return Optional.of(this.renderJSON(o));
 *   }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParamProcessor {
	public Class<?> targetClass() default Object.class;

	public Class<? extends Annotation> targetAnnotation() default ParamProcessor.class;
}