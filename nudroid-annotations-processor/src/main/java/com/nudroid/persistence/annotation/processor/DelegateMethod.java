package com.nudroid.persistence.annotation.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateMethod {

    private String name;
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private Set<String> pathPlaceholderNames = new HashSet<String>();
    private Set<String> queryPlaceholder = new HashSet<String>();
    private Uri uri;

    DelegateMethod(ExecutableElement element, Uri uri) {

        this.uri = uri;
        this.name = element.getSimpleName().toString();
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public Set<String> getQueryPlaceholders() {
    
        return Collections.unmodifiableSet(queryPlaceholder);
    }

    public boolean hasUriPlaceholders() {
        return pathPlaceholderNames.size() > 0;
    }

    void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }

    int getUriId() {
      
        return uri.getId();
    }

    void addPathPlaceholder(String value) {

        pathPlaceholderNames.add(value);
    }

    void addQueryPlaceholder(String value) {

        queryPlaceholder.add(value);
    }
}
