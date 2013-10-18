package com.nudroid.annotation.provider.delegate;

/**
 * Flags which parameter from a content provider delegate method will be passed the reference for the content provider's
 * context.
 * 
 * <p/>
 * Example usage:
 * 
 * <pre>
 * import android.content.Context;
 * 
 * &#064;Query(&quot;/users&quot;)
 * public Cursor findUserBySelection(@ContextRef Context context, ...) {
 * 
 *     ...
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public @interface ContextRef {

}
