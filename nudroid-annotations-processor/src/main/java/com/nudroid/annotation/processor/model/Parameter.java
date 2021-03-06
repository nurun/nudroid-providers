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

import com.nudroid.annotation.processor.LoggingUtils;
import com.nudroid.annotation.processor.ProcessorUtils;
import com.nudroid.annotation.processor.UsedBy;
import com.nudroid.annotation.processor.ValidationErrorGatherer;
import com.nudroid.annotation.provider.delegate.ContentUri;
import com.nudroid.annotation.provider.delegate.ContentValuesRef;
import com.nudroid.annotation.provider.delegate.ContextRef;
import com.nudroid.annotation.provider.delegate.PathParam;
import com.nudroid.annotation.provider.delegate.Projection;
import com.nudroid.annotation.provider.delegate.QueryParam;
import com.nudroid.annotation.provider.delegate.Selection;
import com.nudroid.annotation.provider.delegate.SelectionArgs;
import com.nudroid.annotation.provider.delegate.SortOrder;

import java.util.function.Consumer;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Contains metadata of a parameter from a delegate method.
 * <p>
 * Includes information about the parameter:
 * <p>
 * <ul> <li> If it is one of context, projection, selection, selection args, sort order, content values or uri</li> <li>
 * The type of the parameter</li> <li> The key name, placeholder name and placeholder type if this parameter binds to a
 * URi placeholder</li> </ul>
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Parameter {

    private boolean isContext;
    private boolean isProjection;
    private boolean isSelection;
    private boolean isSelectionArgs;
    private boolean isSortOrder;
    private boolean isContentValues;
    private boolean isContentUri;
    private boolean requiresConversion;
    private boolean isPathParam;
    private boolean isQueryParam;
    private String placeholderName;
    private String parameterType;

    private Parameter() {

    }

    /**
     * The parameter is annotated with {@link ContextRef}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isContext() {
        return isContext;
    }

    /**
     * The parameter is annotated with {@link Projection}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isProjection() {
        return isProjection;
    }

    /**
     * The parameter is annotated with {@link Selection}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isSelection() {
        return isSelection;
    }

    /**
     * The parameter is annotated with {@link SelectionArgs}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isSelectionArgs() {
        return isSelectionArgs;
    }

    /**
     * The parameter is annotated with {@link SortOrder}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isSortOrder() {
        return isSortOrder;
    }

    /**
     * The parameter is annotated with {@link ContentValuesRef}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isContentValues() {
        return isContentValues;
    }

    /**
     * The parameter is a annotated with {@link ContentUri}.
     *
     * @return <tt>true</tt> if annotated, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isContentUri() {
        return isContentUri;
    }

    /**
     * Checks if the parameters needs conversion from String.
     *
     * @return <tt>true</tt> if it does, <tt>false</tt> otherwise
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean getRequiresConversion() {

        return requiresConversion;
    }

    /**
     * The parameter binds a path parameter.
     *
     * @return <tt>true</tt> if it does, <tt>false</tt> otherwise.
     */
    public boolean isPathParameter() {
        return isPathParam;
    }

    /**
     * The parameter binds a query parameter.
     *
     * @return <tt>true</tt> if it does, <tt>false</tt> otherwise.
     */
    public boolean isQueryParameter() {
        return isQueryParam;
    }

    /**
     * The parameter binds to a path or query parameter.
     *
     * @return <tt>true</tt> if it does, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isUriParameter() {
        return isPathParam || isQueryParam;
    }

    /**
     * Returns the placeholder name for this parameter.
     *
     * @return the placeholder name
     */
    public String getPlaceholderName() {
        return placeholderName;
    }

    /**
     * Gets the qualified name of the parameter type.
     *
     * @return the qualified name of the parameter type
     */
    public String getParameterType() {
        return parameterType;
    }

    /**
     * Sets if this parameter is annotated with {@link ContextRef}.
     */
    public void setContext() {

        this.isContext = true;
    }

    /**
     * Sets if this parameter is annotated with {@link Projection}.
     */
    public void setProjection() {
        this.isProjection = true;
    }

    /**
     * Sets if this parameter is annotated with {@link Selection}.
     */
    public void setSelection() {
        this.isSelection = true;
    }

    /**
     * Sets if this parameter is annotated with {@link SelectionArgs}.
     */
    public void setSelectionArgs() {
        this.isSelectionArgs = true;
    }

    /**
     * Sets if this parameter is annotated with {@link SortOrder}.
     */
    public void setSortOrder() {
        this.isSortOrder = true;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentValuesRef}.
     */
    //TODO will be required for update(). Remove SuppressWarnings when so.
    @SuppressWarnings("UnusedDeclaration")
    public void setContentValues() {
        this.isContentValues = true;
    }

    /**
     * Sets if this parameter is annotated with {@link ContentUri}.
     */
    public void setContentUri() {
        this.isContentUri = true;
    }

    /**
     * Sets if this parameter is of type {@link String}.
     */
    public void setRequiresConversion() {
        this.requiresConversion = true;
    }

    private void setPathParam() {
        this.isPathParam = true;
    }

    private void setQueryParam() {
        this.isQueryParam = true;
    }

    /**
     * Sets this parameter placeholder name.
     *
     * @param mPlaceholderName
     *         the placeholder name to set.
     */
    public void setUriVariableName(String mPlaceholderName) {
        this.placeholderName = mPlaceholderName;
    }

    /**
     * Sets the qualified name of the parameter type.
     *
     * @param mParameterType
     *         The qualified name of the type of the parameter.
     */
    public void setParameterType(String mParameterType) {
        this.parameterType = mParameterType;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "isContext=" + isContext +
                ", isProjection=" + isProjection +
                ", isSelection=" + isSelection +
                ", isSelectionArgs=" + isSelectionArgs +
                ", isSortOrder=" + isSortOrder +
                ", isContentValues=" + isContentValues +
                ", isContentUri=" + isContentUri +
                ", requiresConversion=" + requiresConversion +
                ", isPathParam=" + isPathParam +
                ", isQueryParam=" + isQueryParam +
                ", placeholderName='" + placeholderName + '\'' +
                ", parameterType='" + parameterType + '\'' +
                '}';
    }

    /**
     * Builder pattern.
     */
    public static class Builder implements ModelBuilder<Parameter> {

        public static final String ANDROID_CONTEXT_CLASS_NAME = "android.content.Context";
        public static final String ANDROID_URI_CLASS_NAME = "android.net.Uri";

        private VariableElement variableElement;

        /**
         * Initializes the builder.
         *
         * @param variableElement
         *         the required variable element to get metadata from
         */
        public Builder(VariableElement variableElement) {

            this.variableElement = variableElement;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Parses the metadata from the executable element into a Parameter object.
         */
        public Parameter build(ProcessorUtils processorUtils, Consumer<ValidationErrorGatherer> errorCallback) {

            ValidationErrorGatherer gatherer = new ValidationErrorGatherer();
            Parameter parameter = new Parameter();

            TypeMirror parameterType = variableElement.asType();

            if (variableElement.getAnnotation(ContextRef.class) != null) {

                if (processorUtils.isAndroidContext(parameterType)) {

                    parameter.setContext();
                } else {

                    gatherer.gatherError(String.format("Parameters annotated with @%s must be of type %s.",
                                    ContextRef.class.getSimpleName(), ANDROID_CONTEXT_CLASS_NAME), variableElement,
                            LoggingUtils.LogLevel.ERROR);
                }
            }

            if (variableElement.getAnnotation(Projection.class) != null) {

                if (processorUtils.isArrayOfStrings(parameterType)) {

                    parameter.setProjection();
                } else {

                    gatherer.gatherError(String.format("Parameters annotated with @%s must be of type array of %s.",
                                    Projection.class.getSimpleName(), String.class.getName()), variableElement,
                            LoggingUtils.LogLevel.ERROR);
                }
            }

            if (variableElement.getAnnotation(Selection.class) != null) {

                if (processorUtils.isString(parameterType)) {

                    parameter.setSelection();
                } else {

                    gatherer.gatherError(String.format("Parameters annotated with @%s must be of type %s.",
                                    Selection.class.getSimpleName(), String.class.getName()), variableElement,
                            LoggingUtils.LogLevel.ERROR);
                }
            }

            if (variableElement.getAnnotation(SelectionArgs.class) != null) {

                if (processorUtils.isArrayOfStrings(parameterType)) {

                    parameter.setSelectionArgs();
                } else {

                    gatherer.gatherError(String.format("Parameters annotated with @%s must be of type array of %s.",
                                    SelectionArgs.class.getSimpleName(), String.class.getName()), variableElement,
                            LoggingUtils.LogLevel.ERROR);
                }
            }

            if (variableElement.getAnnotation(SortOrder.class) != null) {

                if (processorUtils.isString(parameterType)) {

                    parameter.setSortOrder();
                } else {

                    gatherer.gatherError(String.format("Parameters annotated with @%s must be of type %s.",
                                    SortOrder.class.getSimpleName(), String.class.getName()), variableElement,
                            LoggingUtils.LogLevel.ERROR);
                }
            }

            if (variableElement.getAnnotation(ContentUri.class) != null) {

                if (processorUtils.isAndroidUri(parameterType)) {

                    parameter.setContentUri();
                } else {

                    gatherer.gatherError(String.format("Parameters annotated with @%s must be of type %s.",
                                    ContentUri.class.getSimpleName(), ANDROID_URI_CLASS_NAME), variableElement,
                            LoggingUtils.LogLevel.ERROR);
                }
            }

            final PathParam pathParam = variableElement.getAnnotation(PathParam.class);

            if (pathParam != null) {

                parameter.setUriVariableName(pathParam.value());
                parameter.setPathParam();

                if (!processorUtils.isString(variableElement.asType())) {

                    parameter.setRequiresConversion();
                }
            }

            final QueryParam queryParam = variableElement.getAnnotation(QueryParam.class);

            if (queryParam != null) {

                parameter.setUriVariableName(queryParam.value());
                parameter.setQueryParam();

                if (!processorUtils.isString(variableElement.asType())) {

                    parameter.setRequiresConversion();
                }
            }

            parameter.setParameterType(variableElement.asType()
                    .toString());

            gatherer.emmitCallbackIfApplicable(errorCallback);

            return parameter;
        }
    }
}
