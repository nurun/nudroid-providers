/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
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

    public boolean isProjection() {
        return projection;
    }

    public boolean isSelection() {
        return selection;
    }

    public boolean isSelectionArgs() {
        return selectionArgs;
    }

    public boolean isSortOrder() {
        return sortOrder;
    }

    public boolean isContentValues() {
        return contentValues;
    }

    public boolean isString() {
        return string;
    }

    public boolean isPathParameter() {
        return pathParam;
    }

    public boolean isQueryParameter() {
        return queryParam;
    }

    public String getPathParamPosition() {
        return pathParamPosition;
    }

    public boolean isContentUri() {
        return contentUri;
    }

    public String getQueryParameterName() {
        return queryParameterName;
    }

    void setProjection(boolean projection) {
        this.projection = projection;
    }

    void setSelection(boolean selection) {
        this.selection = selection;
    }

    void setSelectionArgs(boolean selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    void setSortOrder(boolean sortOrder) {
        this.sortOrder = sortOrder;
    }

    void setContentValues(boolean contentValues) {
        this.contentValues = contentValues;
    }

    void setString(boolean string) {
        this.string = string;
    }

    void setPathParameter(boolean pathParam) {
        this.pathParam = pathParam;
    }

    void setQueryParameter(boolean queryParam) {
        this.queryParam = queryParam;
    }

    void setPathParamPosition(String pathParamPosition) {
        this.pathParamPosition = pathParamPosition;
    }

    void setContentUri(boolean contentUri) {
        this.contentUri = contentUri;
    }

    void setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
    }

    @Override
    public String toString() {
        return "Parameter [projection=" + projection + ", selection=" + selection + ", selectionArgs=" + selectionArgs
                + ", sortOrder=" + sortOrder + ", contentValues=" + contentValues + ", contentUri=" + contentUri
                + ", string=" + string + ", pathParam=" + pathParam + ", queryParam=" + queryParam
                + ", pathParamPosition=" + pathParamPosition + ", queryParameterName=" + queryParameterName + "]";
    }
}
