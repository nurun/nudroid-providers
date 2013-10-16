package com.nudroid.provider.interceptor;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public abstract class GenericContentProviderInterceptor implements ContentProviderInterceptor {

    public abstract <T> void before(ContentProviderContext<T> context);

    public abstract <T> T after(ContentProviderContext<T> context);

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeQuery(android.net.Uri, java.lang.String[],
     *      java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public void beforeQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        before(new ContentProviderContext<Void>(null, uri, projection, selection, selectionArgs, sortOrder, null));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterQuery(android.database.Cursor,
     *      android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public Cursor afterQuery(Cursor result, Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        return after(new ContentProviderContext<Cursor>(result, uri, projection, selection, selectionArgs, sortOrder,
                null));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeUpdate(android.net.Uri,
     *      android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public void beforeUpdate(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        before(new ContentProviderContext<Void>(null, uri, null, selection, selectionArgs, null, values));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterUpdate(int, android.net.Uri,
     *      android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int afterUpdate(int result, Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return after(new ContentProviderContext<Integer>(result, uri, null, selection, selectionArgs, null, values));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeInsert(android.net.Uri,
     *      android.content.ContentValues)
     */
    @Override
    public void beforeInsert(Uri uri, ContentValues values) {

        before(new ContentProviderContext<Void>(null, uri, null, null, null, null, values));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterInsert(android.net.Uri, android.net.Uri,
     *      android.content.ContentValues)
     */
    @Override
    public Uri afterInsert(Uri result, Uri uri, ContentValues values) {

        return after(new ContentProviderContext<Uri>(result, uri, null, null, null, null, values));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeDelete(android.net.Uri, java.lang.String,
     *      java.lang.String[])
     */
    @Override
    public void beforeDelete(Uri uri, String selection, String[] selectionArgs) {

        before(new ContentProviderContext<Void>(null, uri, null, selection, selectionArgs, null, null));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterDelete(int, android.net.Uri,
     *      java.lang.String, java.lang.String[])
     */
    @Override
    public int afterDelete(int result, Uri uri, String selection, String[] selectionArgs) {

        return after(new ContentProviderContext<Integer>(result, uri, null, selection, selectionArgs, null, null));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#beforeGetType(android.net.Uri)
     */
    @Override
    public void beforeGetType(Uri uri) {

        before(new ContentProviderContext<Void>(null, uri, null, null, null, null, null));
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterGetType(java.lang.String, android.net.Uri)
     */
    @Override
    public String afterGetType(String result, Uri uri) {

        return after(new ContentProviderContext<String>(result, uri, null, null, null, null, null));
    }
}
