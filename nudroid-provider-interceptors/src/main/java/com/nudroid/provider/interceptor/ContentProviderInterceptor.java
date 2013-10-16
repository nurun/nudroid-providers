package com.nudroid.provider.interceptor;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public interface ContentProviderInterceptor {

    public void beforeQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

    public Cursor afterQuery(Cursor result, Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder);

    public void beforeUpdate(Uri uri, ContentValues values, String selection, String[] selectionArgs);

    public int afterUpdate(int result, Uri uri, ContentValues values, String selection, String[] selectionArgs);

    public void beforeInsert(Uri uri, ContentValues values);

    public Uri afterInsert(Uri result, Uri uri, ContentValues values);

    public void beforeDelete(Uri uri, String selection, String[] selectionArgs);

    public int afterDelete(int result, Uri uri, String selection, String[] selectionArgs);

    public void beforeGetType(Uri uri);

    public String afterGetType(String result, Uri uri);
}
