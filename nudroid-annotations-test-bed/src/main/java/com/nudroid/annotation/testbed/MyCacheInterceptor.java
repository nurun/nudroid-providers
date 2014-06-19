package com.nudroid.annotation.testbed;

import com.nudroid.provider.interceptor.ContentProviderContext;
import com.nudroid.provider.interceptor.ProviderInterceptorPoint;
import com.nudroid.provider.interceptor.cache.CacheInterceptor;
import com.nudroid.provider.interceptor.cache.CachingStrategy;
import com.nudroid.provider.interceptor.cache.SynchronizationStrategy;

/**
 * Cache validation interceptor for the cntent service.
 */
public class MyCacheInterceptor extends CacheInterceptor {


    /**
     * Creates an instance of this class.
     *
     * @param remoteUrl The remote url to download data from.
     * @param cacheId   The id of the cache to use.
     */
    public MyCacheInterceptor(String remoteUrl, String cacheId) {
        super(remoteUrl, cacheId);
    }

    @Override
    public SynchronizationStrategy onCreateSynchronizationStrategy(ContentProviderContext context) {
        return null;
    }

    @Override
    public CachingStrategy onCreateCachingStrategy(ContentProviderContext context) {
        return null;
    }

    /**
     * The interceptor annotation to be applied to methods.
     */
    @ProviderInterceptorPoint
    public static @interface Interceptor {

        /**
         * The remote url to call to fetch up to date data.
         */
        String value();
    }
}
