package me.geso.avans.trigger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation represents the method should call as before dispatch trigger.<br>
 * Before dispatch trigger will call at before dispatching.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeDispatchTrigger {

}
