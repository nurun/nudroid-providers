package com.nurun.persistence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type used to define the Authority of a InterceptableContentProvider. This annotation is not required. If
 * not present, the fully qualified provider class name will be used as its authority name.
 * <p/>
 * Example usage:
 * 
 * <pre>
 * &#064;Authority(&quot;my.custom.authority.name&quot;)
 * public abstract class MyProvider extends InterceptableContentProvider {
 * 
 * }
 * </pre>
 * 
 * @author daniel.freitas
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authority {
    /**
     * The name of the authority for the annotated {@link InterceptableContentProvider}
     */
    String value() default "";
}
