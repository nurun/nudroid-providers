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

package com.nudroid.provider.interceptor.cache;

import com.nudroid.provider.interceptor.ContentProviderContext;

/**
 * Synchronizes the cache with a source. This acts as a one way synchronization (i.e. data is downloaded into the app,
 * never the opposite). The synchronization strategy is invoked if the cache is deemed stale by the caching strategy.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public interface SynchronizationStrategy {

    /**
     * Synchronizer the cache with the source.
     * 
     * @param context
     *            a reference to the content provider delegate context for this request.
     * @param remoteUrl
     *            The url of the source to synchronize against.
     * 
     * @return <tt>true</tt> if the contents of the cache changed due to the synchronization, <tt>false</tt> otherwise.
     * 
     */
    boolean synchronize(ContentProviderContext context, String remoteUrl);

    /**
     * 
     * @param context
     *            a reference to the content provider delegate context for this request.
     * @param remoteUrl
     *            The url of the source to synchronize against.
     * @param page
     *            The page to download.
     * @return <tt>true</tt> if the contents of the cache changed due to the synchronization, <tt>false</tt> otherwise.
     */
    boolean downloadPage(ContentProviderContext context, String remoteUrl, int page);

    /**
     * Invoked if an unhandled error is thrown by
     * {@link SynchronizationStrategy#synchronize(ContentProviderContext, String)} or
     * {@link SynchronizationStrategy#downloadPage(ContentProviderContext, String, int)}.
     * 
     * @param context
     *            a reference to the content provider delegate context for this request.
     * @param e
     *            The exception.
     */
    void onError(ContentProviderContext context, Throwable e);
}
