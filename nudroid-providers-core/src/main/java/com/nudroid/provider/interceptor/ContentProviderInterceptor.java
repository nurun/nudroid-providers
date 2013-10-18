package com.nudroid.provider.interceptor;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.nudroid.annotation.provider.delegate.Delete;
import com.nudroid.annotation.provider.delegate.Insert;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.Update;

/**
 * A provider interceptor wraps invocations to content provider delegate methods and allows code to be executed before
 * and after the delegate method is invoked.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public interface ContentProviderInterceptor {

    /**
     * Called before {@link Query} target method is executed.
     * 
     * @param uri
     *            The uri to be passed to the delegate method.
     * @param projection
     *            The projection to be passed to the delegate method.
     * @param selection
     *            The selection to be passed to the delegate method.
     * @param selectionArgs
     *            The selectionArgs to be passed to the delegate method.
     * @param sortOrder
     *            The sortOrder to be passed to the delegate method.
     */
    public void beforeQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

    /**
     * Called after {@link Query} target method is executed.
     * 
     * @param result
     *            The cursor returned by the target method.
     * 
     * @return The original result or a new Cursor to replace the returned value.
     */
    public Cursor afterQuery(Cursor result);

    /**
     * Called before {@link Update} target method is executed.
     * 
     * @param uri
     *            The uri to be passed to the delegate method.
     * @param values
     *            The values to be passed to the delegate method.
     * @param selection
     *            The selection to be passed to the delegate method.
     * @param selectionArgs
     *            The selectionArgs to be passed to the delegate method.
     */
    public void beforeUpdate(Uri uri, ContentValues values, String selection, String[] selectionArgs);

    /**
     * Called after {@link Update} target method is executed.
     * 
     * @param result
     *            The integer returned by the target method.
     * @return The original result or a new integer to replace the returned value.
     */
    public int afterUpdate(int result);

    /**
     * Called before {@link Insert} target method is executed.
     * 
     * @param uri
     *            The uri to be passed to the delegate method.
     * @param values
     *            The values to be passed to the delegate method.
     */
    public void beforeInsert(Uri uri, ContentValues values);

    /**
     * Called after {@link Insert} target method is executed.
     * 
     * @param result
     *            The Uri returned by the target method.
     * @return The original result or a new Uri to replace the returned value.
     */
    public Uri afterInsert(Uri result);

    /**
     * Called before {@link Delete} target method is executed.
     * 
     * @param uri
     *            The uri to be passed to the delegate method.
     * @param selection
     *            The selection to be passed to the delegate method.
     * @param selectionArgs
     *            The selectionArgs to be passed to the delegate method.
     */
    public void beforeDelete(Uri uri, String selection, String[] selectionArgs);

    /**
     * Called after {@link Delete} target method is executed.
     * 
     * @param result
     *            The integer returned by the target method.
     * @return The original result or a new integer to replace the returned value.
     */
    public int afterDelete(int result);
}
