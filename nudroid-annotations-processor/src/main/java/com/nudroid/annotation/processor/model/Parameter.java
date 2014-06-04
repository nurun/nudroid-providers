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

package com.nudroid.annotation.processor.model;

import com.nudroid.annotation.provider.delegate.ContentUri;
import com.nudroid.annotation.provider.delegate.ContentValuesRef;
import com.nudroid.annotation.provider.delegate.ContextRef;
import com.nudroid.annotation.provider.delegate.Projection;
import com.nudroid.annotation.provider.delegate.Selection;
import com.nudroid.annotation.provider.delegate.SelectionArgs;
import com.nudroid.annotation.provider.delegate.SortOrder;
import com.nudroid.annotation.provider.delegate.UriPlaceholder;

/**
 * Represents a parameter accepted by a delegate method.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Parameter {

    private boolean mContext;
    private boolean mProjection;
    private boolean mSelection;
    private boolean mSelectionArgs;
    private boolean mSortOrder;
    private boolean mContentValues;
    private boolean mContentUri;
    private boolean mString;
    private String mKeyName;
    private String mPlaceholderName;
    private String mParameterType;
    private UriPlaceholderType mUriPlaceholderType;

    /**
     * The parameter is annotated with {@link ContextRef}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isContext() {
        return mContext;
    }

    /**
     * The parameter is annotated with {@link Projection}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isProjection() {
        return mProjection;
    }

    /**
     * The parameter is annotated with {@link Selection}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isSelection() {
        return mSelection;
    }

    /**
     * The parameter is annotated with {@link SelectionArgs}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isSelectionArgs() {
        return mSelectionArgs;
    }

    /**
     * The parameter is annotated with {@link SortOrder}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isSortOrder() {
        return mSortOrder;
    }

    /**
     * The parameter is annotated with {@link ContentValuesRef}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isContentValues() {
        return mContentValues;
    }

    /**
     * The parameter is a annotated with {@link ContentUri}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isContentUri() {
        return mContentUri;
    }

    /**
     * The parameter is of type {@link String}.
     *
     * @return <tt>true</tt> if {@link String}, <tt>false</tt> otherwise.
     */
    public boolean isString() {
        return mString;
    }

    /**
     * The parameter is a annotated with {@link UriPlaceholder}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isPathParameter() {
        return mUriPlaceholderType == UriPlaceholderType.PATH_PARAM;
    }

    /**
     * The parameter is a placeholder appearing in the query string of the URL.
     *
     * @return <tt>true</tt> if it is a query param, <tt>false</tt> otherwise.
     */
    public boolean isQueryParameter() {
        return mUriPlaceholderType == UriPlaceholderType.QUERY_PARAM;
    }

    /**
     * For path parameters, gets the position in the URI path this parameter maps to.
     *
     * @return The position in the URI path this parameter maps to.
     */
    public String getKeyName() {
        return mKeyName;
    }

    /**
     * Returns the placeholder name for this parameter.
     *
     * @return The placeholder name.
     */
    public String getPlaceholderName() {
        return mPlaceholderName;
    }

    /**
     * Get's this parameter type's qualified name.
     *
     * @return This parameter type's name.
     */
    public String getParameterType() {
        return mParameterType;
    }

    /**
     * Sets if this parameter is annotated with {@link ContextRef}.
     *
     * @param isContext
     *         If the parameter is annotated or not.
     */
    public void setContext(boolean isContext) {

        this.mContext = isContext;
    }

    /**
     * Sets if this parameter is annotated with {@link Projection}.
     *
     * @param isProjection
     *         If the parameter is annotated or not.
     */
    public void setProjection(boolean isProjection) {
        this.mProjection = isProjection;
    }

    /**
     * Sets if this parameter is annotated with {@link Selection}.
     *
     * @param isSelection
     *         If the parameter is annotated or not.
     */
    public void setSelection(boolean isSelection) {
        this.mSelection = isSelection;
    }

    /**
     * Sets if this parameter is annotated with {@link SelectionArgs}.
     *
     * @param isSelectionArgs
     *         If the parameter is annotated or not.
     */
    public void setSelectionArgs(boolean isSelectionArgs) {
        this.mSelectionArgs = isSelectionArgs;
    }

    /**
     * Sets if this parameter is annotated with {@link SortOrder}.
     *
     * @param isSortOrder
     *         If the parameter is annotated or not.
     */
    public void setSortOrder(boolean isSortOrder) {
        this.mSortOrder = isSortOrder;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentValuesRef}.
     *
     * @param isContentValues
     *         If the parameter is annotated or not.
     */
    public void setContentValues(boolean isContentValues) {
        this.mContentValues = isContentValues;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentUri}.
     *
     * @param isContentUri
     *         If the parameter is annotated or not.
     */
    public void setContentUri(boolean isContentUri) {
        this.mContentUri = isContentUri;
    }

    /**
     * Sets if this parameter is of type {@link String}.
     *
     * @param isString
     *         If the parameter is of type {@link String}.
     */
    public void setString(boolean isString) {
        this.mString = isString;
    }

    /**
     * Sets the path placeholder position in the URI this parameter maps to.
     *
     * @param keyName
     *         The position in the URI's path.
     */
    public void setKeyName(String keyName) {
        this.mKeyName = keyName;
    }

    /**
     * Sets this parameter placeholder name.
     *
     * @param mPlaceholderName
     *         the placeholder name to set.
     */
    public void setPlaceholderName(String mPlaceholderName) {
        this.mPlaceholderName = mPlaceholderName;
    }

    /**
     * Sets the parameter type's name.
     *
     * @param mParameterType
     *         The type of the parameter.
     */
    public void setParameterType(String mParameterType) {
        this.mParameterType = mParameterType;
    }

    /**
     * Sets the type of placeholder for this parameter.
     *
     * @param placeholderType
     *         The type of placeholder.
     */
    public void setUriPlaceholderType(UriPlaceholderType placeholderType) {
        this.mUriPlaceholderType = placeholderType;
    }

    /**
     * <p></p> {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Parameter [mContext=" + mContext + ", mProjection=" + mProjection + ", mSelection=" + mSelection +
                ", mSelectionArgs=" + mSelectionArgs + ", mSortOrder=" + mSortOrder + ", mContentValues=" +
                mContentValues + ", mContentUri=" + mContentUri + ", mString=" + mString + ", mKeyName=" + mKeyName +
                ", mParamType=" + mUriPlaceholderType + "]";
    }

}
