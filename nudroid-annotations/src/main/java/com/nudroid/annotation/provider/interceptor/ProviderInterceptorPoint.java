package com.nudroid.annotation.provider.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Flags an annotation as an interceptor point for content provider delegates.
 * <p/>
 * Annotations annotated with {@link ProviderInterceptorPoint} can be used to provide implementations of content
 * provider interceptors as well as applying those interceptors to delegate methods. Here's an example usage:
 * <p/>
 * 
 * <pre>
 * //#Creates the interceptor point called Log
 * //##################################
 * &#064;ProviderInterceptorPoint
 * public &#064;interface Log {
 *     String level() default &quot;&quot;;
 * }
 * 
 * //Implementes the log interceptor
 * //################################
 * import android.content.ContentValues;
 * import android.database.Cursor;
 * import android.net.Uri;
 * 
 * import com.nudroid.provider.interceptor.ContentProviderInterceptor;
 * 
 * &#064;Log
 * public class LogInterceptor implements ContentProviderInterceptor {
 * 
 *     public void beforeQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 *         // Logs the event.
 *     }
 * 
 *     public Cursor afterQuery(Cursor result, Uri uri, String[] projection, String selection, String[] selectionArgs,
 *             String sortOrder) {
 *         //Replace result by a decorated cursor with added functionalities
 *         //This demonstrates how the results from the delegate method can be changed before being returned.
 *         return new PotentialNewCursor(result);
 *     }
 * 
 *     public void beforeUpdate(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 *         ...
 *     }
 * 
 *     public int afterUpdate(int result, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 *         ...
 *     }
 * 
 *     public void beforeInsert(Uri uri, ContentValues values) {
 *         ...
 *     }
 * 
 *     public Uri afterInsert(Uri result, Uri uri, ContentValues values) {
 *         ...
 *     }
 * 
 *     public void beforeDelete(Uri uri, String selection, String[] selectionArgs) {
 *         ...
 *     }
 * 
 *     public int afterDelete(int result, Uri uri, String selection, String[] selectionArgs) {
 *         ...
 *     }
 * 
 *     public void beforeGetType(Uri uri) {
 *         ...
 *     }
 * 
 *     public String afterGetType(String result, Uri uri) {
 *         ...
 *     }
 * }
 * 
 * //#Apply the Log interceptor to a delegate method
 * //###############################################
 * &#064;ContentProviderDelegate(authority = "authority")
 * public class ProviderDelegate {
 * 
 *     &#064;Log
 *     &#064;Query("/users/{id}")
 *     public Cursor listAllUsers(@Projection String[] projection, @PathParam("id") String userId) {
 * 
 *         ...
 *     }
 * }
 * 
 * </pre>
 * 
 * The query delegate method annotated with the Log annotation will then be intercepted by the LogInterceptor which will
 * execute the relevant before and after methods around listAllUsers().
 * <p/>
 * <tt>beforeXXX</tt> methods will receive all parameters that will be passed to the delegate method. The interceptor
 * can inspect and even change these parameters and the changes will be carried over to the delegate method. Similarly,
 * <tt>afterXXX</tt> methods will receive these parameters plus the result returned by the delegate method. The
 * interceptor can change this result by returning another object from the <tt>afterXXX</tt> methods.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target(ElementType.ANNOTATION_TYPE)
public @interface ProviderInterceptorPoint {

}
