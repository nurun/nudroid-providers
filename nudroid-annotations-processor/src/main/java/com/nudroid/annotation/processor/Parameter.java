package com.nudroid.annotation.processor;

import com.nudroid.annotation.provider.delegate.ContentUri;
import com.nudroid.annotation.provider.delegate.ContentValuesRef;
import com.nudroid.annotation.provider.delegate.PathParam;
import com.nudroid.annotation.provider.delegate.Projection;
import com.nudroid.annotation.provider.delegate.QueryParam;
import com.nudroid.annotation.provider.delegate.Selection;
import com.nudroid.annotation.provider.delegate.SelectionArgs;
import com.nudroid.annotation.provider.delegate.SortOrder;

/**
 * Represents a parameter accepted by a delegate method.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Parameter {

    private boolean mProjection;
    private boolean mSelection;
    private boolean mSelectionArgs;
    private boolean mSortOrder;
    private boolean mContentValues;
    private boolean mContentUri;
    private boolean mString;
    private boolean mPathParam;
    private boolean mQueryParam;
    private String mPathParamPosition;
    private String mQueryParameterName;

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
     * The parameter is a annotated with {@link PathParam}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isPathParameter() {
        return mPathParam;
    }

    /**
     * The parameter is annotated with {@link QueryParam}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isQueryParameter() {
        return mQueryParam;
    }

    /**
     * For path parameters, gets the position in the URI path this parameter maps to.
     * 
     * @return The position in the URI path this parameter maps to.
     */
    public String getPathParamPosition() {
        return mPathParamPosition;
    }

    /**
     * For query parameters, gets the query parameter name in the URI query string this parameter maps to.
     * 
     * @return The query parameter name in the URI query string this parameter maps to.
     */
    public String getQueryParameterName() {
        return mQueryParameterName;
    }

    /**
     * Sets if this parameter is annotated with {@link Projection}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setProjection(boolean projection) {
        this.mProjection = projection;
    }

    /**
     * Sets if this parameter is annotated with {@link Selection}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    void setSelection(boolean selection) {
        this.mSelection = selection;
    }

    /**
     * Sets if this parameter is annotated with {@link SelectionArgs}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    void setSelectionArgs(boolean selectionArgs) {
        this.mSelectionArgs = selectionArgs;
    }

    /**
     * Sets if this parameter is annotated with {@link SortOrder}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    void setSortOrder(boolean sortOrder) {
        this.mSortOrder = sortOrder;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentValuesRef}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    void setContentValues(boolean contentValues) {
        this.mContentValues = contentValues;
    }

    /**
	 * Sets if this parameter is annotated with {@link ContentUri}.
	 * 
	 * @param mProjection
	 *            If the parameter is annotated or not.
	 */
	void setContentUri(boolean contentUri) {
	    this.mContentUri = contentUri;
	}

	/**
     * Sets if this parameter is of type {@link String}.
     * 
     * @param mProjection
     *            If the parameter is of type {@link String}.
     */
    void setString(boolean string) {
        this.mString = string;
    }

    /**
     * Sets if this parameter is annotated with {@link PathParam}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    void setPathParameter(boolean pathParam) {
        this.mPathParam = pathParam;
    }

    /**
     * Sets if this parameter is annotated with {@link QueryParam}.
     * 
     * @param mProjection
     *            If the parameter is annotated or not.
     */
    void setQueryParameter(boolean queryParam) {
        this.mQueryParam = queryParam;
    }

    /**
     * Sets the path placeholder position in the URI this parameter maps to.
     * 
     * @param pathParamPosition
     *            The position in the URI's path.
     */
    void setPathParamPosition(String pathParamPosition) {
        this.mPathParamPosition = pathParamPosition;
    }

    /**
     * Sets the parameter name in the URI's query string this parameter maps to.
     * 
     * @param queryParameterName
     *            The name of the parameter in the URI's query string.
     */
    void setQueryParameterName(String queryParameterName) {
        this.mQueryParameterName = queryParameterName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Parameter [projection=" + mProjection + ", selection=" + mSelection + ", selectionArgs=" + mSelectionArgs
                + ", sortOrder=" + mSortOrder + ", contentValues=" + mContentValues + ", contentUri=" + mContentUri
                + ", string=" + mString + ", pathParam=" + mPathParam + ", queryParam=" + mQueryParam
                + ", pathParamPosition=" + mPathParamPosition + ", queryParameterName=" + mQueryParameterName + "]";
    }
}
