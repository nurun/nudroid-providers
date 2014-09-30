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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class RedundantSubjectContentProvider_ extends ContentProvider {

    private RedundantContentProviderDelegateSubjectRouter_ mContentProviderRouter;

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public boolean onCreate() {

        final testee.RedundantContentProviderDelegateSubject delegate =
                new testee.RedundantContentProviderDelegateSubject();
        mContentProviderRouter = new RedundantContentProviderDelegateSubjectRouter_(delegate);

        return true;
    }

    @Override
    public Cursor query(Uri contentUri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        return mContentProviderRouter.query(getContext(), contentUri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public Uri insert(Uri contentUri, ContentValues contentValues) {

        return mContentProviderRouter.insert(getContext(), contentUri, contentValues);
    }

    @Override
    public int update(Uri contentUri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return mContentProviderRouter.update(getContext(), contentUri, contentValues, selection, selectionArgs);
    }

    @Override
    public int delete(Uri contentUri, String selection, String[] selectionArgs) {
        return mContentProviderRouter.delete(getContext(), contentUri, selection, selectionArgs);
    }

    @Override
    public String getType(Uri contentUri) {
        return mContentProviderRouter.getType(getContext(), contentUri);
    }
}