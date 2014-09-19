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
 * <p>Registers a content provider delegate. A ContentProvider class will be automatically generated and will forward
 * methods to the annotated delegate class.</p>
 *
 * <p>Example usage:</p>
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
@Target({ElementType.TYPE})
@Documented
public @interface ContentProvider {

    /**
     * The name of the authority to be handled by the generated ContentProvider.
     *
     * @return The name of the authority to be handled by the generated ContentProvider
     */
    String authority();
}
