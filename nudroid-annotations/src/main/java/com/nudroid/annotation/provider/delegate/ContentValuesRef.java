package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Flags which parameter from a content provider delegate method will be passed the reference for the ContentValues
 * instance provided to the original content provider method.
 * <p/>
 * This annotation can only be used in conjunction with {@link Update} or {@link Insert} annotated methods.
 * 
 * <p/>
 * Example usage:
 * 
 * <pre>
 * import android.content.ContentValues;
 * import import android.net.Uri;
 * 
 * &#064;Insert(&quot;/users&quot;)
 * public Uri insertUser(@ContentValuesRef ContentValues contentValuesPassedToTheInsertMethodInTheContentProvider, ...) {
 * 
 *     ...
 * }
 * 
 * &#064;Update(&quot;/users&quot;)
 * public int updateUser(@ContentValuesRef ContentValues contentValuesPassedToTheUpdateMethodInTheContentProvider, ...) {
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
public @interface ContentValuesRef {

}