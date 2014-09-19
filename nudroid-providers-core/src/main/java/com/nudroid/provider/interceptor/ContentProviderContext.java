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

package com.nudroid.provider.interceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

/**
 * A parameter object used by {@link ContentProviderInterceptor}s. Provides access to all possible delegate method
 * parameters. An interceptor can change the properties on this class and the new values will be forwarded to the the
 * content provider delegate method.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class ContentProviderContext {

    private static String PLACEHOLDER_REG_EXP = "\\{([^\\}]+)\\}";
    private static Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REG_EXP);

    /**
     * The content provider context.
     */
    public Context context;

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
    public ContentProviderContext(Context context, Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder, ContentValues contentValues) {

        this.context = context;
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
        return "ContentProviderContext [context=" + context + ", uri=" + uri + ", projection="
                + Arrays.toString(projection) + ", selection=" + selection + ", selectionArgs="
                + Arrays.toString(selectionArgs) + ", sortOrder=" + sortOrder + ", contentValues=" + contentValues
                + ", placeholders=" + placeholders + "]";
    }

    /**
     * Expands the passed string by replacing placeholders (in the form {&lt;placeholder_name&gt;}) by the values captured in
     * property map <tt>placeholders</tt>
     * 
     * @param stringToExpand
     *            The string to be expanded.
     * @return The original string with placeholders replaced by their corresponding values.
     */
    public String expand(String stringToExpand) {

        String result = stringToExpand;

        Matcher m = PLACEHOLDER_PATTERN.matcher(result);

        while (m.find()) {

            String placeholderName = m.group(1);

            final String placeholderValue = placeholders.get(placeholderName);

            if (placeholderValue != null) {

                result = result.replaceAll("\\{" + placeholderName + "\\}", placeholderValue);
            }
        }

        return result;
    }

    /**
     * Expands the passed array of string by replacing placeholders (in the form {&lt;placeholder_name&gt;}) by the values
     * captured in property map <tt>placeholders</tt>
     * 
     * @param stringsToExpand
     *            The array of strings to be expanded. Each string in the array will be expanded individually.
     * @return A new array with the expanded content of each string.
     */
    public String[] expand(String[] stringsToExpand) {

        String[] result = new String[stringsToExpand.length];

        for (int i = 0; i < stringsToExpand.length; i++) {
            result[i] = expand(stringsToExpand[i]);
        }

        return result;
    }
}
