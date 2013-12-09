package com.nudroid.provider.interceptor;

import android.database.Cursor;
import android.net.Uri;

/**
 * A no op adapter implementation of interface {@link ContentProviderInterceptor}. Use this class to simplify
 * implementation of content provider interceptors.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public abstract class ContentProviderInterceptorAdapter implements ContentProviderInterceptor {

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeQuery(com.nudroid.provider.interceptor.ContentProviderContext)
     */
    @Override
    public void beforeQuery(ContentProviderContext contentProviderContext) {

    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterQuery(com.nudroid.provider.interceptor.ContentProviderContext,
     *      android.database.Cursor)
     */
    @Override
    public Cursor afterQuery(ContentProviderContext contentProviderContext, Cursor result) {

        return result;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeUpdate(com.nudroid.provider.interceptor.ContentProviderContext)
     */
    @Override
    public void beforeUpdate(ContentProviderContext contentProviderContext) {

    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterUpdate(com.nudroid.provider.interceptor.ContentProviderContext,
     *      int)
     */
    @Override
    public int afterUpdate(ContentProviderContext contentProviderContext, int result) {

        return result;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeInsert(com.nudroid.provider.interceptor.ContentProviderContext)
     */
    @Override
    public void beforeInsert(ContentProviderContext contentProviderContext) {

    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterInsert(com.nudroid.provider.interceptor.ContentProviderContext,
     *      android.net.Uri)
     */
    @Override
    public Uri afterInsert(ContentProviderContext contentProviderContext, Uri result) {

        return result;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeDelete(com.nudroid.provider.interceptor.ContentProviderContext)
     */
    @Override
    public void beforeDelete(ContentProviderContext contentProviderContext) {

    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterDelete(com.nudroid.provider.interceptor.ContentProviderContext,
     *      int)
     */
    @Override
    public int afterDelete(ContentProviderContext contentProviderContext, int result) {

        return result;
    }
}
