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
     * The parameter is annotated with {@link QueryParam}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
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
     * Sets if this parameter is annotated with {@link ContextRef}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    public void setContext(boolean context) {

        this.mContext = context;
    }

    /**
     * Sets if this parameter is annotated with {@link Projection}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    public void setProjection(boolean projection) {
        this.mProjection = projection;
    }

    /**
     * Sets if this parameter is annotated with {@link Selection}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    public void setSelection(boolean selection) {
        this.mSelection = selection;
    }

    /**
     * Sets if this parameter is annotated with {@link SelectionArgs}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    public void setSelectionArgs(boolean selectionArgs) {
        this.mSelectionArgs = selectionArgs;
    }

    /**
     * Sets if this parameter is annotated with {@link SortOrder}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    public void setSortOrder(boolean sortOrder) {
        this.mSortOrder = sortOrder;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentValuesRef}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    public void setContentValues(boolean contentValues) {
        this.mContentValues = contentValues;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentUri}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    public void setContentUri(boolean contentUri) {
        this.mContentUri = contentUri;
    }

    /**
     * Sets if this parameter is of type {@link String}.
     * 
     * @param mProjection
     *            If the parameter is of type {@link String}.
     */
    public void setString(boolean string) {
        this.mString = string;
    }

    /**
     * Sets the path placeholder position in the URI this parameter maps to.
     * 
     * @param keyName
     *            The position in the URI's path.
     */
    public void setKeyName(String keyName) {
        this.mKeyName = keyName;
    }

    /**
     * Sets the type of placeholder for this parameter.
     * 
     * @param placeholderType
     *            The type of placeholder.
     */
    public void setUriPlaceholderType(UriPlaceholderType placeholderType) {
        this.mUriPlaceholderType = placeholderType;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Parameter [mContext=" + mContext + ", mProjection=" + mProjection + ", mSelection=" + mSelection
                + ", mSelectionArgs=" + mSelectionArgs + ", mSortOrder=" + mSortOrder + ", mContentValues="
                + mContentValues + ", mContentUri=" + mContentUri + ", mString=" + mString + ", mKeyName=" + mKeyName
                + ", mParamType=" + mUriPlaceholderType + "]";
    }

}
