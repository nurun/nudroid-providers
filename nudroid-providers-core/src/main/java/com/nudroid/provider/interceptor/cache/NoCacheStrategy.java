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
