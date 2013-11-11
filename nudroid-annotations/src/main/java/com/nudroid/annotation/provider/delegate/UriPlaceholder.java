package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Binds a URI placeholder to one of the parameters on the delegate method. Placeholder are identified as a name
 * enclosed in braces ({}). This name can be used to bind values in the content URI to parameters in the delegate
 * method.
 * <p/>
 * Example usage:
 * 
 * <pre>
 * &#064;ContentProvider(authority = &quot;com.example.userscontentprovider&quot;)
 * public class UsersContentProviderDelegate {
 *     &#064;Delete(&quot;/users/{user_id}?cascade={cascade_type}&quot;)
 *     public int deleteUser(@UriPlaceholder(&quot;user_id&quot;) String userId, @UriPlaceholder(&quot;cascade_type&quot;) String cascadeType) {
 * 
 *         ...
 *     }
 * }
 * </pre>
 * 
 * A content delete request for URI content://com.example.userscontentprovider/users/daniel.freitas?cascade=1ST_LEVEL
 * will be matched by the deleteUser method above and the userId parameter will be passed the string
 * <tt>daniel.freitas</tt> while the cascadeType parameter will be passed the string <tt>1ST-LEVEL</tt>.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.PARAMETER })
@Documented
public @interface UriPlaceholder {

    /**
     * Mandatory. The name of the placeholder in the path URL.
     */
    String value();
}
