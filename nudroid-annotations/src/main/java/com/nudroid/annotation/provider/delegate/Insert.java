package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a method responsible to handle an insert operation sent to a content provider.
 * <p/>
 * 
 * Methods annotated with Insert must return the URI for the newly inserted item.
 * <p/>
 * 
 * Example usage:
 * 
 * <pre>
 * &#064;Authority(&quot;com.example.userscontentprovider&quot;)
 * public class UsersContentProviderDelegate {
 *     &#064;Insert(&quot;/users&quot;)
 *     public Uri insertUser(@ContentValuesRef ContentValues contentValues) {
 * 
 *         ...
 *     }
 * }
 * </pre>
 * 
 * The above class and method definitions will respond the following content path URLs:
 * content://com.example.userscontentprovider/users.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Insert {

    /**
     * Mandatory. The content path relative to the the content provider authority name.
     */
    String value();
}
