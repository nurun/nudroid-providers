package com.nudroid.provider.interceptor.cache;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.nudroid.provider.interceptor.ContentProviderContext;

/**
 * A caching strategy which uses an age to validate caches against staleness. If the cache is older than the specified
 * max age, the cache is deemed stale.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class MaxAgeCacheStrategy implements CachingStrategy {

    private Clock mClock;
    private long mTimeToLive;
    private TimeUnit mTimeUnit;

    /**
     * Creates an instance of this class.
     * 
     * @param clock
     *            The clock instance used to get the current time.
     * @param maxAge
     *            The maximum age of the cache.
     * @param timeUnit
     *            The time unit for the age.
     */
    public MaxAgeCacheStrategy(Clock clock, long maxAge, TimeUnit timeUnit) {

        this.mClock = clock;
        this.mTimeToLive = maxAge;
        this.mTimeUnit = timeUnit;
    }

    /**
     * Checks the current time and calculates the cache age. If age is greater than the provided max age (in the
     * appropriate time unit) the cache is deemed stale.
     * <p/>
     * {@inheritDoc}
     * 
     * @see CachingStrategy#isUpToDate(ContentProviderContext, String)
     */
    @Override
    public boolean isUpToDate(ContentProviderContext context, String cacheId) {

        SharedPreferences preferences = context.context.getSharedPreferences(cacheId, Context.MODE_PRIVATE);

        long lastUpdateDate = preferences.getLong("VERSION", 0);

        long currentDateAndTime = mClock.currentTime();
        long age = mTimeUnit.convert(currentDateAndTime - lastUpdateDate, TimeUnit.MILLISECONDS);

        return age <= mTimeToLive;
    }

    /**
     * Stores the new version date of the cache.
     * <p/>
     * {@inheritDoc}
     * 
     * @see CachingStrategy#cacheUpdateFinished(ContentProviderContext, String, boolean)
     */
    @Override
    public void cacheUpdateFinished(ContentProviderContext context, String cacheId, boolean wasUpdated) {

        if (wasUpdated) {

            SharedPreferences preferences = context.context.getSharedPreferences(cacheId, Context.MODE_PRIVATE);

            final long version = mClock.currentTime();

            Editor editor = preferences.edit();
            editor.putLong("VERSION", version);
            editor.commit();
        }
    }
}
