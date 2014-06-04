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
 * Skip cache validation. This strategy will always allow the {@link SynchronizationStrategy} to attempt to synchronize
 * the database with the remote server.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class NoCacheStrategy implements CachingStrategy {

    /**
     * Skips cache validation, allowing the {@link SynchronizationStrategy} to always execute.
     * <p/>
     * {@inheritDoc}
     * 
     * @see CachingStrategy#isUpToDate(ContentProviderContext, String)
     */
    @Override
    public boolean isUpToDate(ContentProviderContext context, String cacheId) {

        return true;
    }

    /**
     * No-op.
     * <p/>
     * {@inheritDoc}
     * 
     * @see CachingStrategy#cacheUpdateFinished(ContentProviderContext, String, boolean)
     */
    @Override
    public void cacheUpdateFinished(ContentProviderContext context, String cacheId, boolean wasUpdated) {

    }
}
