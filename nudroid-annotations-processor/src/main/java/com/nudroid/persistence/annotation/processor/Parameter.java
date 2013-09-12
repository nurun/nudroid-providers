/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class Parameter {

    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isProjection() {
        return projection;
    }

    public void setProjection(boolean projection) {
        this.projection = projection;
    }

    public boolean isSelection() {
        return selection;
    }

    public void setSelection(boolean selection) {
        this.selection = selection;
    }

    public boolean isSelectionArgs() {
        return selectionArgs;
    }

    public void setSelectionArgs(boolean selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    public boolean isSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(boolean sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isContentValues() {
        return contentValues;
    }

    public void setContentValues(boolean contentValues) {
        this.contentValues = contentValues;
    }

    public boolean isString() {
        return string;
    }

    public void setString(boolean string) {
        this.string = string;
    }

    public boolean isPathParameter() {
        return pathParam;
    }

    public void setPathParameter(boolean pathParam) {
        this.pathParam = pathParam;
    }

    public boolean isQueryParameter() {
        return queryParam;
    }

    public void setQueryParameter(boolean queryParam) {
        this.queryParam = queryParam;
    }

    public String getPathParamPosition() {
        return pathParamPosition;
    }

    public void setPathParamPosition(String pathParamPosition) {
        this.pathParamPosition = pathParamPosition;
    }

    public boolean isContentUri() {
        return contentUri;
    }

    public void setContentUri(boolean contentUri) {
        this.contentUri = contentUri;
    }

    @Override
    public String toString() {
        return "Parameter [name=" + name + ", projection=" + projection + ", selection=" + selection
                + ", selectionArgs=" + selectionArgs + ", sortOrder=" + sortOrder + ", contentValues=" + contentValues
                + ", contentUri=" + contentUri + ", string=" + string + ", pathParam=" + pathParam + ", queryParam="
                + queryParam + ", pathParamPosition=" + pathParamPosition + "]";
    }

    public String getQueryParameterName() {
        return queryParameterName;
    }

    public void setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
    }
}
