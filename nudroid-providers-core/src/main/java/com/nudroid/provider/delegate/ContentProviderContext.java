package com.nudroid.provider.delegate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.nudroid.provider.interceptor.ContentProviderInterceptor;

/**
 * A parameter object for the {@link ContentProviderInterceptor}. Provides access to all possible delegate method
 * parameters. An interceptor can change the properties on this class and the new values will be forwarded to the the
 * content provider delegate method.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class ContentProviderContext {

    /**
     * The content provider context.
     */
    public Context context;

    /**
     * The {@link SQLiteOpenHelper} instance returned by {@link ContentProviderDelegate#onCreateOpenHelper(Context)}
     */
    public SQLiteOpenHelper sqliteOpenHelper;

    /**
     * The requested content URI.
     */
    public Uri uri;

    /**
     * The projection argument.
     */
    public String[] projection;

    /**
     * The selection argument.
     */
    public String selection;

    /**
     * The arguments for the selection.
     */
    public String[] selectionArgs;

    /**
     * The sort order argument.
     */
    public String sortOrder;

    /**
     * The content values argument.
     */
    public ContentValues contentValues;

    /**
     * The map of parameters intended to replace placeholders in the content UIR.
     */
    public Map<String, String> placeholders = new HashMap<String, String>();

    /**
     * Creates an instance of this parameter object.
     * 
     * @param context
     *            The content provider context.
     * @param openHelper
     *            The {@link SQLiteOpenHelper} instance returned by
     *            {@link ContentProviderDelegate#onCreateOpenHelper(Context)}
     * @param uri
     *            The URI passed to the delegate method.
     * @param projection
     *            The projection parameter passed to the query delegate method.
     * @param selection
     *            The selection parameter passed to the query, update or delete delegate method.
     * @param selectionArgs
     *            The selection parameter passed to the query, update or delete delegate method.
     * @param sortOrder
     *            The sortOrder parameter passed to the query delegate method.
     * @param contentValues
     *            The contentValues parameter passed to the query delegate method.
     */
    public ContentProviderContext(Context context, SQLiteOpenHelper openHelper, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, ContentValues contentValues) {

        this.context = context;
        this.sqliteOpenHelper = openHelper;
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
        this.contentValues = contentValues;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ContentProviderContext [context=" + context + ", sqliteOpenHelper=" + sqliteOpenHelper + ", uri=" + uri
                + ", projection=" + Arrays.toString(projection) + ", selection=" + selection + ", selectionArgs="
                + Arrays.toString(selectionArgs) + ", sortOrder=" + sortOrder + ", contentValues=" + contentValues
                + ", placeholders=" + placeholders + "]";
    }
}
