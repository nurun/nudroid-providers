package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Flags which parameter from a content provider delegate method will be passed the reference for the Uri provided to
 * the original content provider method.
 * <p/>
 * This annotation bears meaning only on {@link Query}, {@link Update}, {@link Insert} or {@link Delete} annotated
 * methods.
 * 
 * <p/>
 * Example usage:
 * 
 * <pre>
 * import import android.net.Uri;
 * 
 * &#064;Query(&quot;/users&quot;)
 * public Cursor listUsers(@ContentUri Uri uriPassedToTheQueryMethodInTheContentProvider) {
 * 
 *     ...
 * }
 * 
 * &#064;Update(&quot;/users&quot;)
 * public int updateUser(@ContentUri Uri uriPassedToTheUpdateMethodInTheContentProvider, ...) {
 * 
 *     ...
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.PARAMETER })
@Documented
public @interface ContentUri {

}
