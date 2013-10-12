package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import com.nudroid.annotation.processor.Parameter;
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

    private String name;
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private Set<String> pathPlaceholderNames = new HashSet<String>();
    private Set<String> queryStringParameterNames = new HashSet<String>();
    private DelegateUri uri;
    private ExecutableElement executableElement;
    private List<Element> interceptorElements = new ArrayList<Element>();

    /**
     * Creates an instance of this class.
     * 
     * @param element
     *            The element representing this delegate method.
     * @param uri
     *            An {@link DelegateUri} describing the URI the method should match.
     */
    public DelegateMethod(ExecutableElement element, DelegateUri uri) {

        this.uri = uri;
        this.name = element.getSimpleName().toString();
        this.executableElement = element;
    }

    /**
     * Gets the name of the method (i.e. method name without return type nor oarameter).
     * 
     * @return The method name.
     */
    public String getName() {
    	
        return name;
    }

    /**
     * Gets the list of parameters this method accepts.
     * 
     * @return List of parameters this method accepts.
     */
    public List<Parameter> getParameters() {
    	
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Gets the names of the query string parameters on the query string on the delegate annotation of this method.
     * 
     * @return The set of query string parameter names.
     */
    public Set<String> getQueryStringParameterNames() {

        return Collections.unmodifiableSet(queryStringParameterNames);
    }

    /**
     * Checks if this method URI has any placeholders in it's path.
     * 
     * @return <tt>true</tt> if this method has any placeholder in its URI path, <tt>false</tt> otherwise.
     */
    public boolean hasUriPlaceholders() {

        return pathPlaceholderNames.size() > 0;
    }

    /**
     * Adds a parameter definition to the list of parameters this method accepts. Parameters added to this method are
     * not checked for validity (ex: duplicate names).
     * 
     * @param parameter
     *            The parameter to add.
     */
    public void addParameter(Parameter parameter) {

        this.parameters.add(parameter);
    }

    /**
     * Get's the URI id assigned to this method's URI in an Android <a
     * href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>.
     * 
     * @return the URI id assigned to this method's URI.
     */
    public int getUriId() {

        return uri.getmId();
    }

    /**
     * Adds a path placeholder name to the list of path placeholders of this method. This method is idempotent.
     * 
     * @param placehorderName
     *            The name of the path placeholder.
     */
    public void addPathPlaceholder(String placehorderName) {

        pathPlaceholderNames.add(placehorderName);
    }

    /**
     * Adds a query string parameter name to the list of query string parameters of this method. This method is
     * idempotent.
     * 
     * @param queryStringParameterName
     *            The name of the query string parameter.
     */
    void addQueryStringParameter(String queryStringParameterName) {

        queryStringParameterNames.add(queryStringParameterName);
    }

    /**
     * Sets the query parameter names for the URI mapped to this method.
     * 
     * @param queryParameterNames
     *            The set of query parameter names.
     */
    public void setQueryParameterNames(Set<String> queryParameterNames) {

        this.queryStringParameterNames = queryParameterNames;
    }

    public ExecutableElement getExecutableElement() {

        return executableElement;
    }

    /**
     * @param interceptorElements
     */
    public void addInterceptors(List<Element> interceptorElements) {

        this.interceptorElements.addAll(interceptorElements);
    }

    public List<Element> getInterceptorElements() {

        return Collections.unmodifiableList(interceptorElements);
    }

    @Override
    public String toString() {
        return "DelegateMethod [name=" + name + ", parameters=" + parameters + "]";
    }
}
