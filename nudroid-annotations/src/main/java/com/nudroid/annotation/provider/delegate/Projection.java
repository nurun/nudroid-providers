package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Flags which parameter from a content provider delegate method will be passed the reference for the String array
 * instance containing the projection provided to the original content provider method.
 * <p/>
 * This annotation bears meaning only on {@link Query} annotated methods.
 * 
 * <p/>
 * Example usage:
 * 
 * <pre>
 * import import android.net.Uri;
 * 
 * &#064;Query(&quot;/users&quot;)
 * public Cursor listUsers(@Projection String[] projection, ...) {
 * 
 *     ...
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.PARAMETER })
@Documented
public @interface Projection {

}
