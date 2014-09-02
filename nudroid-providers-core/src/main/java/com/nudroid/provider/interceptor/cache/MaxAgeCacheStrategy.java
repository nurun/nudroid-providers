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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.nudroid.provider.interceptor.ContentProviderContext;

import java.util.concurrent.TimeUnit;

/**
 * A caching strategy which uses an age to validate caches against staleness. If the cache is older than the specified
 * max age, the cache is deemed stale.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class MaxAgeCacheStrategy implements CachingStrategy {

    private static final String CACHE_VERSION_SUFFIX = "_CACHE_VERSION";

    private Clock mClock;
    private long mTimeToLive;
    private TimeUnit mTimeUnit;

    /**
     * Creates an instance of this class.
     *
     * @param clock
     *         The clock instance used to get the current time.
     * @param maxAge
     *         The maximum age of the cache.
     * @param timeUnit
     *         The time unit for the age.
     */
    public MaxAgeCacheStrategy(Clock clock, long maxAge, TimeUnit timeUnit) {

        this.mClock = clock;
        this.mTimeToLive = maxAge;
        this.mTimeUnit = timeUnit;
    }

    /**
     * Checks the current time and calculates the cache age. If age is greater than the provided max age (in the
     * appropriate time unit) the cache is deemed stale. {@inheritDoc}
     *
     * @see CachingStrategy#isUpToDate(ContentProviderContext, String)
     */
    @Override
    public boolean isUpToDate(ContentProviderContext context, String cacheId) {

        SharedPreferences preferences =
                context.context.getSharedPreferences(CacheInterceptor.CACHE_PAGINATION_PREFERENCES_FILE,
                        Context.MODE_PRIVATE);

        long lastUpdateDate = preferences.getLong(cacheId + CACHE_VERSION_SUFFIX, 0);

        long currentDateAndTime = mClock.currentTime();
        long age = mTimeUnit.convert(currentDateAndTime - lastUpdateDate, TimeUnit.MILLISECONDS);

        return age <= mTimeToLive;
    }

    /**
     * Stores the new version date of the cache. {@inheritDoc}
     *
     * @see CachingStrategy#cacheUpdateFinished(ContentProviderContext, String, boolean)
     */
    @Override
    public void cacheUpdateFinished(ContentProviderContext context, String cacheId, boolean wasUpdated) {

        if (wasUpdated) {

            SharedPreferences preferences =
                    context.context.getSharedPreferences(CacheInterceptor.CACHE_PAGINATION_PREFERENCES_FILE,
                            Context.MODE_PRIVATE);

            final long version = mClock.currentTime();

            Editor editor = preferences.edit();
            editor.putLong(cacheId + CACHE_VERSION_SUFFIX, version);
            editor.commit();
        }
    }

    /**
     * Clears the metadata for the cached content. This will not clear the actual cached data, just the accompanying
     * metadata, forcing a cache update.
     *
     * @param context
     *         an android context to access.
     */
    public static void clearCacheMetadata(Context context) {

        SharedPreferences preferences =
                context.getSharedPreferences(CacheInterceptor.CACHE_PAGINATION_PREFERENCES_FILE, Context.MODE_PRIVATE);

        Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
