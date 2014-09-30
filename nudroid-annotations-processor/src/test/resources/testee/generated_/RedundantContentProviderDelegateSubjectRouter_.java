/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package testee.generated_;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.nudroid.provider.interceptor.ContentProviderContext;

public class RedundantContentProviderDelegateSubjectRouter_ {

    static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    }

    private testee.RedundantContentProviderDelegateSubject mDelegate;

    public RedundantContentProviderDelegateSubjectRouter_(testee.RedundantContentProviderDelegateSubject delegate) {

        this.mDelegate = delegate;
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    public Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        ContentProviderContext contentProviderContext;
        Cursor result;

        switch (URI_MATCHER.match(uri)) {
            default:

                throw new IllegalArgumentException(
                        String.format("@Query URI %s is not mapped by content provider delegate %s", uri,
                                mDelegate.getClass()));
        }
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    public int update(Context context, Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        ContentProviderContext contentProviderContext = null;
        int result = 0;

        switch (URI_MATCHER.match(uri)) {
            default:

                throw new IllegalArgumentException(
                        String.format("Update URI %s is not mapped by content provider delegate %s", uri,
                                mDelegate.getClass()));
        }
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    public Uri insert(Context context, Uri uri, ContentValues values) {

        return null;
    }


    @SuppressWarnings({"unused", "UnusedAssignment"})
    public int delete(Context context, Uri uri, String selection, String[] selectionArgs) {

        return 0;
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    public String getType(Context context, Uri uri) {

        return null;
    }

    @SuppressWarnings({"unused", "unchecked"})
    private <T> T convert(String queryParameter, Class<T> clazz) {

        if (clazz.equals(Character.class) || clazz.equals(char.class)) {
            return (T) Integer.valueOf(Integer.parseInt(queryParameter));
        }

        if (clazz.equals(Character.class) || clazz.equals(char.class)) {
            return (T) Integer.valueOf(Integer.parseInt(queryParameter));
        }

        if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
            return (T) Integer.valueOf(Integer.parseInt(queryParameter));
        }

        if (clazz.equals(Long.class) || clazz.equals(long.class)) {
            return (T) Long.valueOf(Long.parseLong(queryParameter));
        }

        if (clazz.equals(Float.class) || clazz.equals(float.class)) {
            return (T) Float.valueOf(Float.parseFloat(queryParameter));
        }

        if (clazz.equals(Double.class) || clazz.equals(double.class)) {
            return (T) Double.valueOf(Double.parseDouble(queryParameter));
        }

        throw new IllegalArgumentException(
                String.format("Unable to convert string '%s' to type %s.", queryParameter, clazz.getName()));
    }
}