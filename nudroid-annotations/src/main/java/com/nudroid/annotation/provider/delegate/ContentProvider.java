package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a content provider delegate. A ContentProvider will be automatically generated and will forward methods to
 * the annotated delegate.
 * <p/>
 * Example usage:
 * 
 * <pre>
 * &#064;ContentProviderDelegate(authority=&quot;com.example.contentprovider.users&quot;)
 * public class UserContentProviderDelegate {
 *     ...
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ContentProvider {

    /**
     * The name of the authority to be handled by the generated ContentProvider.
     */
    String authority();
}