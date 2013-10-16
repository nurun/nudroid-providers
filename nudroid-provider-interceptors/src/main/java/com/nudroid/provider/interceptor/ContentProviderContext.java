package com.nudroid.provider.interceptor;

import java.util.Arrays;

import android.content.ContentValues;
import android.net.Uri;

/**
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
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @param contentValues
     */
    public ContentProviderContext(T result, Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder, ContentValues contentValues) {

        this.result = result;
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
        this.contentValues = contentValues;
    }

    @Override
    public String toString() {
        return "ContentProviderContext [uri=" + uri + ", projection=" + Arrays.toString(projection) + ", selection="
                + selection + ", selectionArgs=" + Arrays.toString(selectionArgs) + ", sortOrder=" + sortOrder
                + ", contentValues=" + contentValues + ", result=" + result + "]";
    }
}
