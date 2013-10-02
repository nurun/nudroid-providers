package com.nudroid.persistence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a query string placeholder to one of the parameters on the delegate method.
 * <p/>
 * Example usage:
 * 
 * <pre>
 * &#064;Authority(&quot;com.example.userscontentprovider&quot;)
 * public class UsersContentProviderDelegate {
 *     &#064;Delete(&quot;/users?userId={user_id}&quot;)
 *     public int deleteUser(@QueryParam(&quot;user_id&quot;) String userId) {
 * 
 *         ...
 *     }
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface QueryParam {

    /**
     * Mandatory. The name of the placeholder in the query string.
     */
    String value();
}
