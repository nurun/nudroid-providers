package com.nudroid.persistence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Flags which parameter from a content provider delegate method will be passed the reference for the selection String
 * provided to the original content provider method.
 * <p/>
 * This annotation can only be used in conjunction with {@link Query}, {@link Update} or {@link Delete} annotated
 * methods.
 * 
 * <p/>
 * Example usage:
 * 
 * <pre>
 * import import android.net.Uri;
 * 
 * &#064;Query(&quot;/users&quot;)
 * public Cursor findUserBySelection(@Selection String selection, ...) {
 * 
 *     ...
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Selection {

}
