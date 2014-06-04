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

package com.nudroid.annotation.provider.delegate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Denotes a method responsible for handling a query operation sent to a content provider.
 * <p/>
 * 
 * Methods annotated with Query must return a Cursor (or null) object.
 * <p/>
 * 
 * Example usage:
 * 
 * <pre>
 * &#064;ContentProvider(authority=&quot;com.example.userscontentprovider&quot;)
 * public class UsersContentProviderDelegate {
 *      &#064;Query(&quot;/users&quot;)
 *      public Cursor listUsers(...) {
 * 
 *          ...
 *      }
 * }
 * </pre>
 * 
 * The above class and method definitions will respond the following content path URLs:
 * <tt>content://com.example.userscontentprovider/users</tt>.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ ElementType.METHOD })
@Documented
public @interface Query {

    /**
     * Mandatory. The content path relative to the the content provider authority name.
     */
    String value();
}
