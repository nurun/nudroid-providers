package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//TODO. With the changes to how query params are matched against delegate methods. make sure functionality is not
//broken (for corner cases) and that the documentation is updated accordingly (number of params no longer taken into
//account. checks only query names ordered by qyery parameter count, descending. It can match 2 methods but which one
//of the two it will pick can't be determined).
//Example:
//contentUri1=XXX?param1={}&param2={}&param3={}
//contentUri2=XXX?param1={}&param2={}&param4={}
//The content uri contentUri1=XXX?param1=1&param2=2&param3=3&param4=4 can match both of them, but the first one to be
//tested against will be used. Previously, it would match none since it also validated that the number of query params
//was the same
/**
 * Binds a URI placeholder to one of the parameters on the delegate method.
 * <p/>
 * Example usage:
 * 
 * <pre>
 * &#064;Authority(&quot;com.example.userscontentprovider&quot;)
 * public class UsersContentProviderDelegate {
 *     &#064;Delete(&quot;/users/{user_id}?filter={filter_type}&quot;)
 *     public int deleteUser(@UriPlaceholder(&quot;user_id&quot;) String userId, @UriPlaceholder(&quot;filter_type&quot;) String filterType) {
 * 
 *         ...
 *     }
 * }
 * </pre>
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
