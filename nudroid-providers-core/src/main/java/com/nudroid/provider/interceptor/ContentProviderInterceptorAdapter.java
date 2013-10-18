package com.nudroid.provider.interceptor;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * A no op adapter interface {@link ContentProviderInterceptor}.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public abstract class ContentProviderInterceptorAdapter implements ContentProviderInterceptor {

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeQuery(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public void beforeQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

    }

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterQuery(android.database.Cursor)
     */
    @Override
    public Cursor afterQuery(Cursor result) {

        return result;
    }

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeUpdate(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public void beforeUpdate(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

    }

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterUpdate(int)
     */
    @Override
    public int afterUpdate(int result) {

        return result;
    }

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeInsert(android.net.Uri, android.content.ContentValues)
     */
    @Override
    public void beforeInsert(Uri uri, ContentValues values) {

    }

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterInsert(android.net.Uri)
     */
    @Override
    public Uri afterInsert(Uri result) {

        return result;
    }

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeDelete(android.net.Uri, java.lang.String, java.lang.String[])
     */
    @Override
    public void beforeDelete(Uri uri, String selection, String[] selectionArgs) {

    }

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterDelete(int)
     */
    @Override
    public int afterDelete(int result) {

        return result;
    }
}
