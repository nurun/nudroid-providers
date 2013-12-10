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
