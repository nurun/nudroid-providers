package com.nudroid.annotation.processor.model;

import javax.lang.model.element.ExecutableElement;

/**
 * A mapping between a {@link DelegateUri} and the delegate methods.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class UriMethodDelegateMapper {

    private DelegateUri mDelegateUri;
    private ExecutableElement mQueryMethod;
    private ExecutableElement mUpdateMethod;
    private ExecutableElement mInsertMethod;
    private ExecutableElement mDeleteMethod;

    /**
     * Creates an instance of the class.
     * 
     * @param delegateUri
     *            The {@link DelegateUri} to map the methods to.
     */
    public UriMethodDelegateMapper(DelegateUri delegateUri) {

        this.mDelegateUri = delegateUri;
    }

    /**
     * Gets the delegate uri instance.
     * 
     * @return The delegate uri instance.
     */
    public DelegateUri getDelegateUri() {
        return mDelegateUri;
    }

    /**
     * Gets the query method mapped to this URI.
     * 
     * @return The query method mapped to this URI.
     */
    public ExecutableElement getQueryMethod() {
        return mQueryMethod;
    }

    /**
     * Sets the query method mapped to this URI.
     * 
     * @param queryMethod
     *            The query method to map.
     */
    public void setQueryMethod(ExecutableElement queryMethod) {
        this.mQueryMethod = queryMethod;
    }

    /**
     * Gets the update method mapped to this URI.
     * 
     * @return The update method mapped to this URI.
     */
    public ExecutableElement getUpdateMethod() {
        return mUpdateMethod;
    }

    /**
     * Sets the update method mapped to this URI.
     * 
     * @param updateMethod
     *            The update method to map.
     */
    public void setUpdateMethod(ExecutableElement updateMethod) {
        this.mUpdateMethod = updateMethod;
    }

    /**
     * Gets the insert method mapped to this URI.
     * 
     * @return The insert method mapped to this URI.
     */
    public ExecutableElement getInsertMethod() {
        return mInsertMethod;
    }

    /**
     * Sets the insert method mapped to this URI.
     * 
     * @param insertMethod
     *            The query method to map.
     */
    public void setInsertMethod(ExecutableElement insertMethod) {
        this.mInsertMethod = insertMethod;
    }

    /**
     * Gets the insert method mapped to this URI.
     * 
     * @return The insert method mapped to this URI.
     */
    public ExecutableElement getDeleteMethod() {
        return mDeleteMethod;
    }

    /**
     * Sets the delete method mapped to this URI.
     * 
     * @param deleteMethod
     *            The query method to map.
     */
    public void setDeleteMethod(ExecutableElement deleteMethod) {
        this.mDeleteMethod = deleteMethod;
    }
}
