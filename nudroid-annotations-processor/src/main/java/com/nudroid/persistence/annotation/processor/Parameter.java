package com.nudroid.persistence.annotation.processor;

import com.nudroid.persistence.annotation.ContentUri;
import com.nudroid.persistence.annotation.ContentValues;
import com.nudroid.persistence.annotation.PathParam;
import com.nudroid.persistence.annotation.Projection;
import com.nudroid.persistence.annotation.QueryParam;
import com.nudroid.persistence.annotation.Selection;
import com.nudroid.persistence.annotation.SelectionArgs;
import com.nudroid.persistence.annotation.SortOrder;

/**
 * Represents a parameter accepted by a delegate method.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Parameter {

    private boolean projection;
    private boolean selection;
    private boolean selectionArgs;
    private boolean sortOrder;
    private boolean contentValues;
    private boolean contentUri;
    private boolean string;
    private boolean pathParam;
    private boolean queryParam;
    private String pathParamPosition;
    private String queryParameterName;

    /**
     * The parameter is annotated with {@link Projection}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isProjection() {
        return projection;
    }

    /**
     * The parameter is annotated with {@link Selection}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isSelection() {
        return selection;
    }

    /**
     * The parameter is annotated with {@link SelectionArgs}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isSelectionArgs() {
        return selectionArgs;
    }

    /**
     * The parameter is annotated with {@link SortOrder}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isSortOrder() {
        return sortOrder;
    }

    /**
     * The parameter is annotated with {@link ContentValues}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isContentValues() {
        return contentValues;
    }

    /**
     * The parameter is of type {@link String}.
     * 
     * @return <tt>true</tt> if {@link String}, <tt>false</tt> otherwise.
     */
    public boolean isString() {
        return string;
    }

    /**
     * The parameter is a annotated with {@link PathParam}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isPathParameter() {
        return pathParam;
    }

    /**
     * The parameter is annotated with {@link QueryParam}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isQueryParameter() {
        return queryParam;
    }

    /**
     * For path parameters, gets the position in the URI path this parameter maps to.
     * 
     * @return The position in the URI path this parameter maps to.
     */
    public String getPathParamPosition() {
        return pathParamPosition;
    }

    /**
     * The parameter is a annotated with {@link ContentUri}.
     * 
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    public boolean isContentUri() {
        return contentUri;
    }

    /**
     * For query parameters, gets the query parameter name in the URI query string this parameter maps to.
     * 
     * @return The query parameter name in the URI query string this parameter maps to.
     */
    public String getQueryParameterName() {
        return queryParameterName;
    }

    /**
     * Sets if this parameter is annotated with {@link Projection}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setProjection(boolean projection) {
        this.projection = projection;
    }

    /**
     * Sets if this parameter is annotated with {@link Selection}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setSelection(boolean selection) {
        this.selection = selection;
    }

    /**
     * Sets if this parameter is annotated with {@link SelectionArgs}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setSelectionArgs(boolean selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    /**
     * Sets if this parameter is annotated with {@link SortOrder}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setSortOrder(boolean sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentValues}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setContentValues(boolean contentValues) {
        this.contentValues = contentValues;
    }

    /**
     * Sets if this parameter is of type {@link String}.
     * 
     * @param projection
     *            If the parameter is of type {@link String}.
     */
    void setString(boolean string) {
        this.string = string;
    }

    /**
     * Sets if this parameter is annotated with {@link PathParam}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setPathParameter(boolean pathParam) {
        this.pathParam = pathParam;
    }

    /**
     * Sets if this parameter is annotated with {@link QueryParam}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setQueryParameter(boolean queryParam) {
        this.queryParam = queryParam;
    }

    /**
     * Sets the path placeholder position in the URI this parameter maps to.
     * 
     * @param pathParamPosition
     *            The position in the URI's path.
     */
    void setPathParamPosition(String pathParamPosition) {
        this.pathParamPosition = pathParamPosition;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentUri}.
     * 
     * @param projection
     *            If the parameter is annotated or not.
     */
    void setContentUri(boolean contentUri) {
        this.contentUri = contentUri;
    }

    /**
     * Sets the parameter name in the URI's query string this parameter maps to.
     * 
     * @param queryParameterName
     *            The name of the parameter in the URI's query string.
     */
    void setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Parameter [projection=" + projection + ", selection=" + selection + ", selectionArgs=" + selectionArgs
                + ", sortOrder=" + sortOrder + ", contentValues=" + contentValues + ", contentUri=" + contentUri
                + ", string=" + string + ", pathParam=" + pathParam + ", queryParam=" + queryParam
                + ", pathParamPosition=" + pathParamPosition + ", queryParameterName=" + queryParameterName + "]";
    }
}
