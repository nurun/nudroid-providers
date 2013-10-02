package com.nudroid.persistence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a method responsible to handle an update operation sent to a content provider.
 * <p/>
 * 
 * Methods annotated with Update must return an integer value denoting the number of rows affected.
 * <p/>
 * 
 * Example usage:
 * 
 * <pre>
 * &#064;Authority(&quot;com.example.userscontentprovider&quot;)
 * public class UsersContentProviderDelegate {
 *     &#064;Update(&quot;/users/{user_id}&quot;)
 *     public int updateUser(@PathParam(&quot;user_id&quot;) String userId, @ContentValuesRef ContentValues contentValues) {
 * 
 *         ...
 *     }
 * }
 * </pre>
 * 
 * The above class and method definitions will respond the following content path URLs:
 * content://com.example.userscontentprovider/users/#.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Update {
    
    /**
     * Mandatory. The content path relative to the the content provider authority name.
     */
    String value();

}
