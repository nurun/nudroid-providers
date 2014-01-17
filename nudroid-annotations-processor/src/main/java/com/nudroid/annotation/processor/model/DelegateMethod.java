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
 * Holds information about the delegate method for a content provider.
 * <p/>
 * Delegate methods are methods annotated with one of the delegate annotations: {@link Query}, {@link Update},
 * {@link Insert}, or {@link Delete}.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateMethod {

    private DelegateClass mDelegateClass;
    private String mName;
    private List<Parameter> mParameters = new ArrayList<Parameter>();
    private List<Parameter> mPathPlaceholderParameters = new ArrayList<Parameter>();
    private List<Parameter> mQueryStringPlaceholderParameters = new ArrayList<Parameter>();
    private Set<String> mQueryStringParameterNames = new HashSet<String>();
    private DelegateUri mUri;
    private ExecutableElement mExecutableElement;
    private List<InterceptorPoint> mInterceptorElements = new ArrayList<InterceptorPoint>();
    private List<InterceptorPoint> mInverseInterceptorElements = null;

    /**
     * Creates an instance of this class.
     * 
     * @param element
     *            The {@link ExecutableElement} representing this delegate method.
     * @param uri
     *            An {@link DelegateUri} describing the URI the method should match.
     */
    public DelegateMethod(ExecutableElement element, DelegateUri uri) {

        this.mUri = uri;
        this.mName = element.getSimpleName().toString();
        this.mExecutableElement = element;
    }

    /**
     * Adds a parameter definition to the list of parameters this method accepts. Parameters added to this method are
     * not checked for validity (ex: duplicate names).
     * 
     * @param parameter
     *            The parameter to add.
     */
    public void addParameter(Parameter parameter) {

        this.mParameters.add(parameter);

        if (parameter.isPathParameter()) {
            mPathPlaceholderParameters.add(parameter);
        }

        if (parameter.isQueryParameter()) {
            mQueryStringPlaceholderParameters.add(parameter);
        }
    }

    /**
     * Adds an interceptor point to this method. Interceptors work as an around advice around the delegate method.
     * 
     * @param interceptor
     *            The interceptor type to add.
     */
    public void addInterceptor(InterceptorPoint interceptor) {

        this.mInterceptorElements.add(interceptor);
    }

    /**
     * Sets the query string parameter names present in the URI mapped for this method.
     * 
     * @param queryStringParameterNames
     *            The set of query string parameter names.
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
    public List<Parameter> getPathPlaceholderParameters() {

        return mPathPlaceholderParameters;
    }

    /**
     * Gets the list of parameters this method accepts mapped to a placeholder in the query string portion of the URL. A
     * subset of {@link #getParameters()}.
     * 
     * @return List of parameters.
     */
    public List<Parameter> getQueryStringPlaceholderParameters() {

        return mQueryStringPlaceholderParameters;
    }

    /**
     * Gets the names of the query string parameters on the query string on the delegate annotation of this method.
     * 
     * @return The set of query string parameter names.
     */
    public Set<String> getQueryStringParameterNames() {

        return mQueryStringParameterNames;
    }

    /**
     * Gets the count of parameters on the query string on the delegate annotation of this method.
     * 
     * @return The count of parameters on the query string.
     */
    public int getQueryStringParameterCount() {

        return mQueryStringParameterNames.size();
    }

    /**
     * Checks if this method URI has any placeholders in it's path.
     * 
     * @return <tt>true</tt> if this method has any placeholder in its URI path, <tt>false</tt> otherwise.
     */
    public boolean hasUriPlaceholders() {

        return mPathPlaceholderParameters.size() > 0;
    }

    /**
     * Checks if this method URI has any placeholders in it's query string.
     * 
     * @return <tt>true</tt> if this method has any placeholder in its query string, <tt>false</tt> otherwise.
     */
    public boolean hasQueryStringPlaceholders() {

        return mQueryStringPlaceholderParameters.size() > 0;
    }

    /**
     * Gets the list of interceptors for this delegate method, in the order they are executed before the delegate
     * invocation.
     * 
     * @return The list of interceptors for this delegate method.
     */
    public List<InterceptorPoint> getBeforeInterceptorList() {

        return mInterceptorElements;
    }

    /**
     * Gets the list of interceptors for this delegate method, in the order they are executed after the delegate
     * Invocation.
     * 
     * @return The list of interceptors for this delegate method.
     */
    public List<InterceptorPoint> getAfterInterceptorList() {

        if (mInverseInterceptorElements == null) {

            mInverseInterceptorElements = new ArrayList<InterceptorPoint>(mInterceptorElements.size());
            mInverseInterceptorElements.addAll(Lists.reverse(mInterceptorElements));
        }

        return mInverseInterceptorElements;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DelegateMethod [mDelegateClass=" + mDelegateClass + ", mName=" + mName + ", mParameters=" + mParameters
                + ", mPathPlaceholderParameters=" + mPathPlaceholderParameters + ", mQueryStringPlaceholderParameters="
                + mQueryStringPlaceholderParameters + ", mQueryStringParameterNames=" + mQueryStringParameterNames
                + ", mUri=" + mUri + ", mExecutableElement=" + mExecutableElement + ", mInterceptorElements="
                + mInterceptorElements + ", mInverseInterceptorElements=" + mInverseInterceptorElements + "]";
    }
}
