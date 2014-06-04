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
 * Defines a caching strategy to be used by a {@link CacheInterceptor}.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public interface CachingStrategy {

    /**
     * Informs if the cache is up to date or not. If the cache is not up to date (<tt>false</tt>), the synchronizer will
     * be invoked.
     * 
     * @param context
     *            a reference to the content provider delegate context for this request.
     * @param cacheId
     *            The id of the cache to check.
     * 
     * @return <tt>true</tt> if the cache is up to date, <tt>false</tt> otherwise.
     */
    boolean isUpToDate(ContentProviderContext context, String cacheId);

    /**
     * Updates the cache metadata. This is invoked immediately after the synchronizer has finished its work so the cache
     * strategy can store meta information about the cache, like new expiration dates etc. This is only invoked if a
     * request to the synchonizer was performed.
     * 
     * @param context
     *            a reference to the content provider delegate context for this request.
     * @param cacheId
     *            The id of the cache to check.
     * @param wasSynchronized
     *            If the synchronizer successfully synchronized the cache.
     */
    void cacheUpdateFinished(ContentProviderContext context, String cacheId, boolean wasSynchronized);
}
