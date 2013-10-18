package com.nudroid.provider.interceptor;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * A generic interceptor which can apply the same aspect to all types of delegate methods.
 * <p/>
 * Extending classes only need to implement the generic required abstract methods before and after.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public abstract class GenericContentProviderInterceptor implements ContentProviderInterceptor {

    /**
     * Called before target delegate method.
     * 
     * @param context
     *            A parameter object with all parameters that are to be passed to the delegate method.
     */
    public abstract <T> void before(ContentProviderContext<T> context);

    /**
     * Called after target delegate method.
     * 
     * @param result
     *            The result returned by the target delegate method.
     * @return The original result or a new result to replace the returned value.
     */
    public abstract <T> T after(T result);

    /**
     * 
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
     * 
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterQuery(android.database.Cursor)
     */
    @Override
    public Cursor afterQuery(Cursor result) {

        return after(null);
    }

    /**
     * 
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
     * 
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterUpdate(int)
     */
    @Override
    public int afterUpdate(int result) {

        return after(null);
    }

    /**
     * 
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
     * 
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterInsert(android.net.Uri)
     */
    @Override
    public Uri afterInsert(Uri result) {

        return after(null);
    }

    /**
     * 
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
     * 
     * <p/>
     * {@inheritDoc}
     * 
     * @see com.nudroid.provider.interceptor.ContentProviderInterceptor#afterDelete(int)
     */
    @Override
    public int afterDelete(int result) {

        return after(null);
    }
}
