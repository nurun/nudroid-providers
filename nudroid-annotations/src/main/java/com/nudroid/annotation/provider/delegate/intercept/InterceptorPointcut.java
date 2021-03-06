/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.nudroid.annotation.provider.delegate.intercept;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * <p>Flags an annotation as an interceptor point for content provider delegates.</p>
 *
 * <p>Annotations annotated with {@link InterceptorPointcut} can be used to apply interceptors to content provider
 * delegate methods. Interceptor annotations must be defined as a static inner class of the corresponding interceptor
 * class.</p>
 *
 * <p>When applied to a delegate method, interceptors will execute code before and after the delegate method, in the order
 * they were annotated in the source code before the delegate invocation and in reverse order they were annotated in the
 * source code after the delegate method invocation, just like an onion or around aspect invocation.</p>
 *
 * <p>Interceptors must implement InterceptorPointcut</p>
 *
 * <p>Interceptor classes must provide either a default constructor or a constructor with a
 * {@link InterceptorPointcut} annotation as a parameter (see example below for more details).</p>
 *
 * <pre>
 * //1 - Create an interceptor for logging
 * //################################
 * import android.database.Cursor;
 * 
 * import com.nudroid.provider.delegate.ContentProviderContext;
 * import com.nudroid.provider.interceptor.InterceptorPointcut;
 * import com.nudroid.annotation.provider.interceptor.InterceptorPointcut;
 * 
 * public class LogInterceptor implements InterceptorPointcut {
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
 * //#2 - Create the InterceptorPointcut annotation
 * //#    and add a constructor
 * //##################################
 * import android.database.Cursor;
 * 
 * import com.nudroid.provider.delegate.ContentProviderContext;
 * import com.nudroid.provider.interceptor.InterceptorPointcut;
 * import com.nudroid.annotation.provider.interceptor.InterceptorPointcut;
 * 
 * public class LogInterceptor implements InterceptorPointcut {
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
 *     &#064;InterceptorPointcut
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
 * <p>The query delegate method annotated with the Log annotation will then be intercepted by the LogInterceptor which will
 * execute the relevant before and after methods around listAllUsers().</p>
 *
 * <p><tt>beforeXXX</tt> methods will receive all parameters that will be passed to the delegate method. The interceptor
 * can inspect and even change these parameters and the changes will be carried over to the delegate method and
 * subsequent interceptors. Similarly, <tt>afterXXX</tt> methods will receive these parameters plus the result returned
 * by the delegate method. The interceptor can change this result by returning another object from the <tt>afterXXX</tt>
 * methods.</p>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target(ElementType.ANNOTATION_TYPE)
public @interface InterceptorPointcut {

}
