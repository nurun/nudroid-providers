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
 * <p>Binds a URI placeholder to one of the parameters on the delegate method. Placeholder are identified as a name
 * enclosed in braces ({}). This name can be used to bind values in the content URI to parameters in the delegate
 * method.</p>
 *
 * <p>Example usage:</p>
 *
 * <pre>
 * &#064;ContentProvider(authority = &quot;com.example.userscontentprovider&quot;)
 * public class UsersContentProviderDelegate {
 *     &#064;Delete(&quot;/users/{user_id}?cascade={cascade_type}&quot;)
 *     public int deleteUser(@UriPlaceholder(&quot;user_id&quot;) String userId, @UriPlaceholder(&quot;cascade_type&quot;)
 * String cascadeType) {
 *
 *         ...
 *     }
 * }
 * </pre>
 *
 * <p>A content delete request for URI <tt>content://com.example.userscontentprovider/users/daniel.freitas?cascade=1ST_LEVEL</tt>
 * will be matched by the <tt>deleteUser</tt> method above and the <tt>userId</tt> parameter will be passed the string
 * <tt>daniel.freitas</tt> while the <tt>cascadeType</tt> parameter will be passed the string <tt>1ST-LEVEL</tt>.</p>
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
@Target({ElementType.PARAMETER})
@Documented
public @interface PathParam {

    /**
     * Mandatory. The name of the placeholder in the path URL.
     *
     * @return The content path relative to the the content provider authority name.
     */
    String value();
}
