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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.nudroid.provider.interceptor.ContentProviderContext;
import com.nudroid.provider.interceptor.ContentProviderInterceptor;
import com.nudroid.provider.interceptor.GenericContentProviderInterceptor;

/**
 * <p>A content provider delegate interceptor which validates persisted data against staleness and updates the data if it
 * is deemed stale before proceeding with the request.</p>
 *
 * <p>Staleness is determined according to a caching strategy that is to be provided by subclasses. See
 * {@link CacheInterceptor#onCreateCachingStrategy(ContentProviderContext)} for more details.</p>
 *
 * <p>Synchronization is provided by a synchronization strategy that is to be provided by subclasses. See
 * {@link CacheInterceptor#onCreateSynchronizationStrategy(ContentProviderContext)} for more details.</p>
 *
 * <p>If the cache is deemed stale by the caching strategy, the synchronizer will be invoked to synchronize the data. All
 * of it happens before the underlying storage is accessed by the content provider so the synchronizer has a chance to
 * download or update the data in the persistent storage before the content provider can access it.</p>
 *
 * <p>Applying a cache interceptor to a content provider delegate follows the normal rules for provider interceptors.
 * Subclasses must include an annotation annotated with ProviderInterceptorPoint which is in turn applied to
 * specific provider delegate methods. Subclasses provide the caching and synchronization strategy to be used and this
 * class takes care of orchestrating the process.</p>

 * <h1>Pagination</h1>
 *
 * <p>Cache interceptors supports pagination of content. If 'com.nudroid.provider.interceptor.cache.pagination' is passed
 * as a query string parameter in the content URI with a value of ALL or NEXT, the method
 * {@link SynchronizationStrategy#downloadPage(ContentProviderContext, String, int)} will be invoked instead. The page
 * number to download will be passed as a parameter and it is an abstraction of the page to download. It will be an
 * incremental integer number. The semantics of page download must be handled by synchronization strategy.</p>
 *
 * <p>***NOTE: *** Page numbers are tracked by cache id. Each cache id has it's own track of downloaded pages. When using
 * pagination, care must be taken when selecting cache ids least a request ends up using pagination information from
 * another URL. The auto assigned cache id (which resolves to the qualified remote url) should be good for most cases
 * .</p>
 *
 * <p>A value of ALL for pagination will validate the cache using the caching strategy and if the cache is stale the
 * download of the first page will be initiated. If cache is up to date, the synchronizatoin is not invoked and the
 * content provider will access whatever data has already been downloaded. This scenario should be used when accessing
 * an activity for the first time so either the first page is downloaded or cahced content is imediatelly served. As the
 * user scrolls to the end of the content (most probably on a list or grid view) requests can be sent with a value of
 * NEXT. This will request the synchronizer to download the next page of data.</p>
 *
 * <p>A value of NEXT never triggers a cache validation.</p>
 *
 * <h1>Example</h1>
 *
 * <p>Here's an example of a caching interceptor.</p>
 * 
 * <pre>
 * package com.example.test_anotations.vision.custom;
 * 
 * import java.util.concurrent.TimeUnit;
 * 
 * import com.example.test_anotations.vision.base.AndroidTimeClock;
 * import com.example.test_anotations.vision.base.CacheInterceptor;
 * import com.example.test_anotations.vision.base.CachingStrategy;
 * import com.example.test_anotations.vision.base.MaxAgeCacheStrategy;
 * import com.example.test_anotations.vision.base.SynchronizationStrategy;
 * import com.nudroid.annotation.provider.interceptor.ProviderInterceptorPoint;
 * import com.nudroid.provider.interceptor.ContentProviderContext;
 * 
 * public class UserFavoritesCacheInterceptor extends CacheInterceptor {
 * 
 *     &#064;ProviderInterceptorPoint
 *     public static @interface Annotation {
 *         String remoteUrl();
 * 
 *         String cacheId() default &quot;&quot;;
 * 
 *         long updateInterval();
 * 
 *         TimeUnit timeUnit();
 * 
 *         String userId();
 *     }
 * 
 *     private static final AndroidTimeClock SYSTEM_CLOCK_SINGLETON = new AndroidTimeClock();
 *     private long mUpdateInterval;
 *     private TimeUnit mTimeUnit;
 *     private String mUserid;
 * 
 *     public UserFavoritesCacheInterceptor(Annotation annotation) {
 * 
 *         super(annotation.remoteUrl(), annotation.cacheId());
 *         this.mUpdateInterval = annotation.updateInterval();
 *         this.mTimeUnit = annotation.timeUnit();
 *         this.mUserid = annotation.userId();
 *     }
 * 
 *     &#064;Override
 *     public CachingStrategy onCreateCachingStrategy(ContentProviderContext context) {
 * 
 *         return new MaxAgeCacheStrategy(SYSTEM_CLOCK_SINGLETON, mUpdateInterval, mTimeUnit);
 *     }
 * 
 *     &#064;Override
 *     public SynchronizationStrategy onCreateSynchronizationStrategy(ContentProviderContext context) {
 * 
 *         return new SynchronizationStrategy() {
 * 
 *             &#064;Override
 *             public boolean synchronize(ContentProviderContext context, String url) {
 *                 // Do whatever it is needed to download/sync data for user mUserId.
 *                 // If successful return true
 *                 return true;
 *             }
 * 
 *             &#064;Override
 *             public boolean downloadPage(ContentProviderContext context, String url, int page) {
 * 
 *                 if (page == 1) {
 *                     // Delete content from database for this query.
 *                     // Download page 1.
 *                     // Persist into storage.
 *                     return true;
 *                 } else {
 *                     // Download next page. Let's suppose results are ordered by date and the backend accepts a date to
 *                     // be passed as a parameter so only results greater than the passed date are returned. In this
 *                     // scenario, the actual page number does not matter.
 * 
 *                     // Get last inserted record date.
 *                     // Call remote server and specify the date as a parameter.
 *                     // Insert new contents in the storage.
 *                     return true;
 *                 }
 *             }
 * 
 *             public void onError(ContentProviderContext context, Throwable e) {
 *                 // Handle error.
 *             }
 *         };
 *     }
 * }
 * </pre>
 * 
 * <p>And how it can be applied to a provider delegate:</p>
 * 
 * <pre>
 * &#064;UserFavoritesCacheInterceptor.Annotation(
 *         remoteUrl = &quot;https://dev-hypnotoad-midlayer.herokuapp.com/api/users/{userId}/favorites&quot;,
 *         updateInterval = 15,
 *         timeUnit = TimeUnit.MINUTES,
 *         userId = &quot;{userId}&quot;)
 * &#064;Query(&quot;/user/{userId}&quot;)
 * public Cursor findUserByX(@UriPlaceholder(&quot;userId&quot;) String userId) {
 * 
 *     return SQLITE_OPEN_HELPER.getReadableDatabase().query(&quot;UserFavorites&quot;,
 *             new String[] { &quot;_id, favoriteId&quot;, &quot;contentId&quot;, &quot;contentType&quot; }, &quot;userId = ?&quot;, new String[] { userId },
 *             null, null, &quot;_id ASC&quot;);
 * }
 * </pre>
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public abstract class CacheInterceptor extends GenericContentProviderInterceptor {

    private static final String CACHE_ID_PROPERTY_NAME = "cacheId";
    private static final String REMOTE_URL_PROPERTY_NAME = "remoteUrl";
    private static final String PAGE_QUERY_STRING_PARAMETER_NAME = "com.nudroid.provider.interceptor.cache.pagination";
    private static final String PAGE_REMOVAL_REG_EXP = "com\\.nudroid\\.provider\\.interceptor\\.cache\\.pagination\\=[^\\&]*(\\&+)?";
    private static Map<String, Semaphore> sSemaphoresForCacheId = new HashMap<String, Semaphore>();

    /**
     * Supported pagination instructions for the synchronizer.
     * 
     * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
     */
    public static enum PaginationType {
        /**
         * Uses whatever content has already been downloaded. Triggers the download of the first page if cache is empty.
         */
        ALL,

        /**
         * Downloads the next page of data.
         */
        NEXT,

        /**
         * Do not paginate results.
         */
        NONE
    }

    private CachingStrategy mCachingStrategy;
    private SynchronizationStrategy mSynchronizationStrategy;

    private String mRemoteUrl;
    private String mCacheId;

    private static String sTag = "CacheInterceptor";

    /**
     * Creates an instance of this class.
     * 
     * @param remoteUrl
     *            The remote url to download data from.
     * 
     * @param cacheId
     *            The id of the cache to use.
     */
    public CacheInterceptor(String remoteUrl, String cacheId) {

        this.mRemoteUrl = remoteUrl;

        if (this.mRemoteUrl == null) {
            Log.d(sTag, String.format(
                    "Parameter %s not set. Remote URL is null. Subclasses must implement getRemoteUrl(Context)",
                    REMOTE_URL_PROPERTY_NAME));
        }

        if (cacheId == null || cacheId.trim().equals("")) {

            this.mCacheId = mRemoteUrl;
        } else {

            this.mCacheId = cacheId;
        }

        if (this.mCacheId == null || this.mCacheId.trim().endsWith("")) {
            Log.d(sTag, String.format(
                    "Parameter %s not set. Cache id is null. Subclasses must implement getCacheId(Context)",
                    CACHE_ID_PROPERTY_NAME));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see ContentProviderInterceptor#onCreate(ContentProviderContext)
     */
    public void onCreate(ContentProviderContext context) {

        mCachingStrategy = onCreateCachingStrategy(context);
        mSynchronizationStrategy = onCreateSynchronizationStrategy(context);
    }

    /**
     * Called when the cache interceptor is being created to provide the strategy to use.
     *
     * @param context A reference to the content provider context
     *
     * @return The caching strategy to use for synchronizing the cache for this request.
     */
    public abstract SynchronizationStrategy onCreateSynchronizationStrategy(ContentProviderContext context);

    /**
     * Called when the cache interceptor is being created to provide the strategy to use.
     *
     * @param context A reference to the content provider context
     * 
     * @return The caching strategy to use for verifying the cache for this request.
     */
    public abstract CachingStrategy onCreateCachingStrategy(ContentProviderContext context);

    /**
     * Gets the remote url to use. By default, returns the remote url passed in the constructor but can be overridden to
     * return something else.
     * 
     * @param context A reference to the content provider context
     * 
     * @return The remote url to use for synchronization.
     */
    public String getRemoteUrl(ContentProviderContext context) {

        return mRemoteUrl;
    }

    /**
     * Gets the cache id to use. By default, returns the cache id passed in the constructor but can be overridden to
     * return something else.
     * 
     * @param context A reference to the content provider context
     * 
     * @return The cache id to use for synchronization.
     */
    public String getCacheId(ContentProviderContext context) {

        return mCacheId;
    }

    /**
     * Orchestrates the logic for checking and updating the cache.
     * {@inheritDoc}
     * 
     * @see GenericContentProviderInterceptor#before(ContentProviderContext)
     */
    public void before(ContentProviderContext context) {

        final String cacheId = Base64.encodeToString(getCacheId(context).getBytes(), Base64.NO_PADDING | Base64.NO_WRAP
                | Base64.URL_SAFE);

        PaginationType paginationType = PaginationType.NONE;
        String paginationTypeName = context.uri.getQueryParameter(PAGE_QUERY_STRING_PARAMETER_NAME);
        context.uri = Uri.parse(context.uri.toString().replaceAll(PAGE_REMOVAL_REG_EXP, ""));

        if (paginationTypeName != null && !paginationTypeName.trim().equals("")) {

            try {

                paginationType = PaginationType.valueOf(paginationTypeName);
            } catch (IllegalArgumentException e) {
                // Ignore and defaults to NONE
            }
        }

        boolean wasInterrupted = false;
        final Semaphore semaphore = getSemaphore(cacheId);

        try {

            semaphore.acquire();
            checkAndUpdateCache(context, cacheId, paginationType);
        } catch (InterruptedException e) {

            wasInterrupted = true;
        } finally {

            if (!wasInterrupted && semaphore.availablePermits() == 0) {

                semaphore.release();
            }
        }
    }

    private Semaphore getSemaphore(String cacheId) {

        synchronized (sSemaphoresForCacheId) {

            Semaphore semaphore = sSemaphoresForCacheId.get(cacheId);

            if (semaphore == null) {
                semaphore = new Semaphore(1);
                sSemaphoresForCacheId.put(cacheId, semaphore);
            }

            return semaphore;
        }
    }

    private void checkAndUpdateCache(ContentProviderContext context, final String cacheId, PaginationType paginationType) {
        switch (paginationType) {
        /*
         * If no pagination is requested, just validate cache and call synchronization is cache is stale.
         */
        case NONE:
            if (!mCachingStrategy.isUpToDate(context, cacheId)) {

                boolean wasSynchronized = false;

                try {
                    wasSynchronized = mSynchronizationStrategy.synchronize(context, getRemoteUrl(context));
                } catch (Throwable e) {
                    mSynchronizationStrategy.onError(context, e);
                }

                if (wasSynchronized) {

                    SharedPreferences preferences = context.context.getSharedPreferences("PAGE_BY_CACHE",
                            Context.MODE_PRIVATE);
                    Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                }

                mCachingStrategy.cacheUpdateFinished(context, cacheId, wasSynchronized);
            }

            break;
        /*
         * If requesting current paginated cache, validate cache and download first page if cache is stale.
         */
        case ALL:

            if (!mCachingStrategy.isUpToDate(context, cacheId)) {

                boolean pageDownloaded = false;

                try {
                    pageDownloaded = mSynchronizationStrategy.downloadPage(context, getRemoteUrl(context), 1);
                } catch (Throwable e) {
                    mSynchronizationStrategy.onError(context, e);
                }

                if (pageDownloaded) {

                    SharedPreferences preferences = context.context.getSharedPreferences("PAGE_BY_CACHE",
                            Context.MODE_PRIVATE);
                    Editor editor = preferences.edit();
                    editor.putInt(cacheId, 1);
                    editor.commit();
                }

                mCachingStrategy.cacheUpdateFinished(context, cacheId, pageDownloaded);
            }

            break;
        /*
         * If downloading the next page, do not validate the cache. Just go ahead and download next page.
         */
        case NEXT:

            boolean pageDownloaded = false;

            SharedPreferences preferences = context.context.getSharedPreferences("PAGE_BY_CACHE", Context.MODE_PRIVATE);
            int currentPage = preferences.getInt(cacheId, 1);

            try {
                pageDownloaded = mSynchronizationStrategy.downloadPage(context, getRemoteUrl(context), currentPage + 1);
            } catch (Throwable e) {
                mSynchronizationStrategy.onError(context, e);
            }

            if (pageDownloaded) {

                Editor editor = preferences.edit();
                editor.putInt(cacheId, currentPage + 1);
                editor.commit();
            }

            break;
        default:
            break;
        }
    }

    /**
     * No-op. There's nothing to do after the content provider gets the results.
     * {@inheritDoc}
     * 
     * @see GenericContentProviderInterceptor#after(ContentProviderContext, Object)
     */
    public <T> T after(ContentProviderContext contentProviderContext, T result) {

        return result;
    }
}
