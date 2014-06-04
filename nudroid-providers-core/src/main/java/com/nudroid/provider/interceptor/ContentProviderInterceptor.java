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

package com.nudroid.provider.interceptor;

import android.database.Cursor;
import android.net.Uri;

/**
 * A provider interceptor wraps invocations to content provider delegate methods and allows code to be executed before
 * and after the delegate method is invoked.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public interface ContentProviderInterceptor {

    /**
     * Called when the insterceptor is instantiated.
     * 
     * @param context
     *            The content provider context.
     */
    public void onCreate(ContentProviderContext context);

    /**
     * Called before com.nudroid.annotation.provider.delegate.Query target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     */
    public void beforeQuery(ContentProviderContext contentProviderContext);

    /**
     * Called after com.nudroid.annotation.provider.delegate.Query target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     * 
     * @param result
     *            The cursor returned by the target method.
     * 
     * @return The original result or a new Cursor to replace the returned value.
     */
    public Cursor afterQuery(ContentProviderContext contentProviderContext, Cursor result);

    /**
     * Called before com.nudroid.annotation.provider.delegate.Update target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     */
    public void beforeUpdate(ContentProviderContext contentProviderContext);

    /**
     * Called after com.nudroid.annotation.provider.delegate.Update target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     * 
     * @param result
     *            The integer returned by the target method.
     * 
     * @return The original result or a new integer to replace the returned value.
     */
    public int afterUpdate(ContentProviderContext contentProviderContext, int result);

    /**
     * Called before com.nudroid.annotation.provider.delegate.Insert target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     */
    public void beforeInsert(ContentProviderContext contentProviderContext);

    /**
     * Called after com.nudroid.annotation.provider.delegate.Insert target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     * 
     * @param result
     *            The Uri returned by the target method.
     * 
     * @return The original result or a new Uri to replace the returned value.
     */
    public Uri afterInsert(ContentProviderContext contentProviderContext, Uri result);

    /**
     * Called before com.nudroid.annotation.provider.delegate.Delete target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     */
    public void beforeDelete(ContentProviderContext contentProviderContext);

    /**
     * Called after com.nudroid.annotation.provider.delegate.Delete target method is executed.
     * 
     * @param contentProviderContext
     *            A parameter object with the parameters that are to be passed to the delegate method.
     * 
     * @param result
     *            The integer returned by the target method.
     * 
     * @return The original result or a new integer to replace the returned value.
     */
    public int afterDelete(ContentProviderContext contentProviderContext, int result);
}
