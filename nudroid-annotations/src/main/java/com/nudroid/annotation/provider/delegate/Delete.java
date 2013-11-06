package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Denotes a method responsible to handle a delete operation sent to a content provider.
 * <p/>
 * 
 * Methods annotated with Delete must return an integer value denoting the number of rows affected.
 * <p/>
 * 
 * Example usage:
 * 
 * <pre>
 * &#064;Authority(&quot;com.example.userscontentprovider&quot;)
 * public class UsersContentProviderDelegate {
 *     &#064;Delete(&quot;/users/{user_id}&quot;)
 *     public int deleteUser(@PathParam(&quot;user_id&quot;) String userId) {
 * 
 *         ...
 *     }
 * }
 * </pre>
 * 
 * The above class and method definitions will respond the following content path URLs:
 * content://com.example.userscontentprovider/users/*.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.METHOD })
@Documented
public @interface Delete {

    /**
     * Mandatory. The content path relative to the the content provider authority name.
     */
    String value();
}
