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
 * A no op adapter implementation of interface {@link ContentProviderInterceptor}. Use this class to simplify
 * implementation of content provider interceptors.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public abstract class ContentProviderInterceptorAdapter implements ContentProviderInterceptor {

    /**
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeQuery(com.nudroid.provider.interceptor.ContentProviderContext)
     */
    @Override
    public void beforeQuery(ContentProviderContext contentProviderContext) {

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterQuery(com.nudroid.provider.interceptor.ContentProviderContext,
     *      android.database.Cursor)
     */
    @Override
    public Cursor afterQuery(ContentProviderContext contentProviderContext, Cursor result) {

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeUpdate(com.nudroid.provider.interceptor.ContentProviderContext)
     */
    @Override
    public void beforeUpdate(ContentProviderContext contentProviderContext) {

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterUpdate(com.nudroid.provider.interceptor.ContentProviderContext,
     *      int)
     */
    @Override
    public int afterUpdate(ContentProviderContext contentProviderContext, int result) {

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeInsert(com.nudroid.provider.interceptor.ContentProviderContext)
     */
    @Override
    public void beforeInsert(ContentProviderContext contentProviderContext) {

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterInsert(com.nudroid.provider.interceptor.ContentProviderContext,
     *      android.net.Uri)
     */
    @Override
    public Uri afterInsert(ContentProviderContext contentProviderContext, Uri result) {

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeDelete(com.nudroid.provider.interceptor.ContentProviderContext)
     */
    @Override
    public void beforeDelete(ContentProviderContext contentProviderContext) {

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterDelete(com.nudroid.provider.interceptor.ContentProviderContext,
     *      int)
     */
    @Override
    public int afterDelete(ContentProviderContext contentProviderContext, int result) {

        return result;
    }
}
