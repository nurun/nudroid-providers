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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;

import com.google.common.collect.Lists;
import com.nudroid.annotation.provider.delegate.Delete;
import com.nudroid.annotation.provider.delegate.Insert;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.Update;

/**
 * <p>Holds information about the delegate method for a content provider.</p>
 * <p>
 * <p>Delegate methods are methods annotated with one of the delegate annotations: {@link Query}, {@link Update}, {@link
 * Insert}, or {@link Delete}.</p>
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateMethod {

    private String mName;
    private List<Parameter> mParameters = new ArrayList<>();
    private List<Parameter> mPathParameters = new ArrayList<>();
    private List<Parameter> mQueryStringParameters = new ArrayList<>();
    private Set<String> mQueryStringParameterNames = new HashSet<>();
    private ExecutableElement mExecutableElement;
    private List<InterceptorPoint> mInterceptorElements = new ArrayList<>();
    private List<InterceptorPoint> mInverseInterceptorElements = null;

    /**
     * Creates an instance of this class.
     *
     * @param element
     *         The {@link javax.lang.model.element.ExecutableElement} representing this delegate method.
     */
    public DelegateMethod(ExecutableElement element) {

        this.mName = element.getSimpleName()
                .toString();
        this.mExecutableElement = element;
    }

    /**
     * Adds a parameter definition to the list of parameters this method accepts. Parameters added to this method are
     * not checked for validity (ex: duplicate names).
     *
     * @param parameter
     *         The parameter to add.
     */
    public void addParameter(Parameter parameter) {

        this.mParameters.add(parameter);

        if (parameter.isPathParameter()) {
            mPathParameters.add(parameter);
        }

        if (parameter.isQueryParameter()) {
            mQueryStringParameters.add(parameter);
        }
    }

    /**
     * Adds an interceptor point to this method. Interceptors work as an around advice around the delegate method.
     *
     * @param interceptor
     *         The interceptor type to add.
     */
    public void addInterceptor(InterceptorPoint interceptor) {

        this.mInterceptorElements.add(interceptor);
    }

    /**
     * Sets the query string parameter names present in the URI mapped for this method.
     *
     * @param queryStringParameterNames
     *         The set of query string parameter names.
     */
    public void setQueryParameterNames(Set<String> queryStringParameterNames) {

        this.mQueryStringParameterNames = queryStringParameterNames;
    }

    /**
     * Gets the {@link ExecutableElement} of the method represented by this class.
     *
     * @return The {@link ExecutableElement} of the method represented by this class.
     */
    public ExecutableElement getExecutableElement() {

        return mExecutableElement;
    }

    /**
     * Gets the name of the method (i.e. method name without return type nor oarameter).
     *
     * @return The method name.
     */
    public String getName() {

        return mName;
    }

    /**
     * Gets the list of parameters this method accepts.
     *
     * @return List of parameters this method accepts.
     */
    public List<Parameter> getParameters() {

        return mParameters;
    }

    /**
     * Gets the list of parameters this method accepts mapped to a placeholder in the path portion or the URL. A subset
     * of {@link #getParameters()}.
     *
     * @return List of parameters.
     */
    @SuppressWarnings("UnusedDeclaration")
    public List<Parameter> getPathParameters() {

        return mPathParameters;
    }

    /**
     * Gets the list of parameters this method accepts mapped to a placeholder in the query string portion of the URL. A
     * subset of {@link #getParameters()}.
     *
     * @return List of parameters.
     */
    @SuppressWarnings("UnusedDeclaration")
    public List<Parameter> getQueryStringParameters() {

        return mQueryStringParameters;
    }

    /**
     * Gets the names of the query string parameters on the query string on the delegate annotation of this method.
     *
     * @return The set of query string parameter names.
     */
    @SuppressWarnings("UnusedDeclaration")
    public Set<String> getQueryStringParameterNames() {

        return mQueryStringParameterNames;
    }

    /**
     * Checks if this method URI has any placeholders in it's path.
     *
     * @return <tt>true</tt> if this method has any placeholder in its URI path, <tt>false</tt> otherwise.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean getHasUriPlaceholders() {

        return mPathParameters.size() > 0;
    }

    /**
     * Gets the list of interceptors for this delegate method, in the order they are executed before the delegate
     * invocation.
     *
     * @return The list of interceptors for this delegate method.
     */
    @SuppressWarnings("UnusedDeclaration")
    public List<InterceptorPoint> getBeforeInterceptorList() {

        return mInterceptorElements;
    }

    /**
     * Gets the list of interceptors for this delegate method, in the order they are executed after the delegate
     * Invocation.
     *
     * @return The list of interceptors for this delegate method.
     */
    @SuppressWarnings("UnusedDeclaration")
    public List<InterceptorPoint> getAfterInterceptorList() {

        if (mInverseInterceptorElements == null) {

            mInverseInterceptorElements = new ArrayList<>(mInterceptorElements.size());
            mInverseInterceptorElements.addAll(Lists.reverse(mInterceptorElements));
        }

        return mInverseInterceptorElements;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        result = prime * result + ((mParameters == null) ? 0 : mParameters.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DelegateMethod other = (DelegateMethod) obj;
        if (mName == null) {
            if (other.mName != null) return false;
        } else if (!mName.equals(other.mName)) return false;
        if (mParameters == null) {
            if (other.mParameters != null) return false;
        } else if (!mParameters.equals(other.mParameters)) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DelegateMethod [mName=" + mName + ", mParameters=" +
                mParameters + ", mPathParameters=" + mPathParameters +
                ", mQueryStringParameters=" + mQueryStringParameters +
                ", mQueryStringParameterNames=" + mQueryStringParameterNames +
                ", mExecutableElement=" + mExecutableElement + ", mInterceptorElements=" + mInterceptorElements +
                ", mInverseInterceptorElements=" + mInverseInterceptorElements + "]";
    }
}
