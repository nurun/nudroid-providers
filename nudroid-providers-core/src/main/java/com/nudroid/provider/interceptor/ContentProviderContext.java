package com.nudroid.provider.interceptor;

import java.util.Arrays;

import android.content.ContentValues;
import android.net.Uri;

/**
 * A parameter object for the {@link GenericContentProviderInterceptor}. Provides access to all possible delegate method
 * parameters.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class ContentProviderContext<T> {

    public final Uri uri;
    public final String[] projection;
    public final String selection;
    public final String[] selectionArgs;
    public final String sortOrder;
    public final ContentValues contentValues;
    public final T result;

    /**
     * Creates an instance of this parameter object.
     * 
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
    ContentProviderContext(T result, Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder, ContentValues contentValues) {

        this.result = result;
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
        this.contentValues = contentValues;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ContentProviderContext [uri=" + uri + ", projection=" + Arrays.toString(projection) + ", selection="
                + selection + ", selectionArgs=" + Arrays.toString(selectionArgs) + ", sortOrder=" + sortOrder
                + ", contentValues=" + contentValues + ", result=" + result + "]";
    }
}
