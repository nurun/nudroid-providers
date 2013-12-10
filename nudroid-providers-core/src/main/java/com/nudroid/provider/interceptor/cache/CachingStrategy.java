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
