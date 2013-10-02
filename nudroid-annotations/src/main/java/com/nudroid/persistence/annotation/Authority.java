package com.nudroid.persistence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define the authority name to be mapped by a content provider delegate.
 * <p/>
 * Example usage:
 * 
 * <pre>
 * &#064;Authority(&quot;com.example.contentprovider.users&quot;)
 * public class UserContentProviderDelegate {
 * 
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Authority {

    /**
     * Optional. The name of the authority of the ContentProvider. This attribute is optional. If not present, the fully
     * qualified name of the annotated class will be used as an Authority name.
     */
    String value() default "";
}
