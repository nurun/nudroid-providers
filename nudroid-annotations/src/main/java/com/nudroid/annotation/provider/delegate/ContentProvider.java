package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Registers a content provider delegate. A ContentProvider class will be automatically generated and will forward
 * methods to the annotated delegate class.
 * <p/>
 * Example usage:
 * 
 * <pre>
 * &#064;ContentProvider(authority=&quot;com.example.contentprovider.users&quot;)
 * public class UserContentProviderDelegate implements ContentProviderDelegate {
 *     ...
 *     
 *     &#064;Override
 *     public boolean onCreate(Context context) {
 * 
 *         // Create/get SQLiteOpenHelper here.
 *         return true;
 *     }
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.TYPE })
@Documented
public @interface ContentProvider {

    /**
     * The name of the authority to be handled by the generated ContentProvider.
     */
    String authority();
}
