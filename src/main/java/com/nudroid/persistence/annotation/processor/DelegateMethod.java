/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class DelegateMethod {

    private int queryPlaceholderCount;
    private String name;
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private Set<String> pathPlaceholderNames = new HashSet<String>();
    private Set<String> queryPlaceholderNames = new HashSet<String>();
    private Uri uri;

    public DelegateMethod(Uri uri) {

        this.uri = uri;
        this.queryPlaceholderCount = uri.getQueryPlaceholderCount();
    }

    public void setName(String methodName) {
        this.name = methodName;

    }

    public void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }

    @Override
    public String toString() {
        return String.format("method('%s')", name);
    }

    public int getUriId() {
        return uri.getId();
    }

    public int getQueryPlaceholderCount() {
        return queryPlaceholderCount;
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public void addPathPlaceholder(String value) {

        pathPlaceholderNames.add(value);
    }

    public Set<String> getPathPlaceholderNames() {
        
        return Collections.unmodifiableSet(pathPlaceholderNames);
    }

    public void addQueryPlaceholder(String value) {
        
        queryPlaceholderNames.add(value);
    }
    
    public Set<String> getQueryPlaceholderNames() {
        
        return Collections.unmodifiableSet(queryPlaceholderNames);
    }
}
