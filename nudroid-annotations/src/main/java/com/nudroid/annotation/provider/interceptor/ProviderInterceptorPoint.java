package com.nudroid.annotation.provider.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Flags an annotation as an interceptor point for content provider delegates.
 * <p/>
 * Annotations annotated with {@link ProviderInterceptorPoint} can be used to apply interceptors to content provider
 * delegate methods. Interceptor annotations must be defined as a static inner class of the corresponding interceptor
 * class.
 * <p/>
 * When applied to a delegate method, interceptors will execute code before and after the delegate method, in the order
 * they were declared in the source code before the delegate invocation and in reverse order they were declared in the
 * source code after the delegate method invocation, just like an onion or around aspect invocation.
 * <p/>
 * Interceptors must implement ContentProviderInterceptor
 * <p/>
 * Interceptor classes must provide either one of a default constructor or a constructor with a
 * {@link ProviderInterceptorPoint} annotation as a parameter (see example below for more details).
 * <p/>
 * 
 * <pre>
 * //1 - Create an interceptor for logging
 * //################################
 * import android.database.Cursor;
 * 
 * import com.nudroid.provider.delegate.ContentProviderContext;
 * import com.nudroid.provider.interceptor.ContentProviderInterceptor;
 * import com.nudroid.annotation.provider.interceptor.ProviderInterceptorPoint;
 * 
 * public class LogInterceptor implements ContentProviderInterceptor {
 * 
 *     private String mLogLevel;
 * 
 *     public void beforeQuery(ContentProviderContext contentProviderContext) {
 *     
 *         // Logs check log level and log the event.
 *     }
 * 
 *     public Cursor afterQuery(ContentProviderContext contentProviderContext, Cursor result) {
 *     
 *         // Don't change the result
 *         return result;
 *     }
 * 
 *     ...
 * }
 * 
 * //#2 - Create the ProviderInterceptorPoint annotation
 * //#    and add a constructor
 * //##################################
 * import android.database.Cursor;
 * 
 * import com.nudroid.provider.delegate.ContentProviderContext;
 * import com.nudroid.provider.interceptor.ContentProviderInterceptor;
 * import com.nudroid.annotation.provider.interceptor.ProviderInterceptorPoint;
 * 
 * public class LogInterceptor implements ContentProviderInterceptor {
 * 
 *     String mLogLevel;
 * 
 *     public LogInterceptor(Log log) {
 *     
 *         this.mLogLevel = log.level();
 *     }
 *     
 *     ...
 *     
 *     &#064;ProviderInterceptorPoint
 *     public &#064;interface Log {
 *         String level() default &quot;TRACE&quot;;
 *     }     
 * }
 * 
 * //#3 - Apply the Log interceptor to a delegate method
 * //###############################################
 * &#064;ContentProviderDelegate(authority = "authority")
 * public class ProviderDelegate {
 * 
 *     &#064;LogInterceptor.Log(level = "INFO")
 *     &#064;Query("/users/{id}")
 *     public Cursor listAllUsers(@Projection String[] projection, @PathParam("id") String userId) {
 * 
 *         ...
 *     }
 * }
 * </pre>
 * 
 * The query delegate method annotated with the Log annotation will then be intercepted by the LogInterceptor which will
 * execute the relevant before and after methods around listAllUsers().
 * <p/>
 * <tt>beforeXXX</tt> methods will receive all parameters that will be passed to the delegate method. The interceptor
 * can inspect and even change these parameters and the changes will be carried over to the delegate method and
 * subsequent interceptors. Similarly, <tt>afterXXX</tt> methods will receive these parameters plus the result returned
 * by the delegate method. The interceptor can change this result by returning another object from the <tt>afterXXX</tt>
 * methods.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target(ElementType.ANNOTATION_TYPE)
public @interface ProviderInterceptorPoint {

}
