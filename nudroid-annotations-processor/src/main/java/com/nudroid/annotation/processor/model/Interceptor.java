package com.nudroid.annotation.processor.model;

import javax.lang.model.element.TypeElement;

/**
 * Information about delegate method interceptors.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Interceptor {

    private TypeElement mInterceptorClassElement;
    private TypeElement mInterceptorAnnotationElement;

    /**
     * Creates a new interceptor.
     * 
     * @param interceptorAnnotationElement
     * @param interceptorClassElement
     *            The type element for this interceptor.
     */
    public Interceptor(TypeElement interceptorAnnotationElement, TypeElement interceptorClassElement) {

        this.mInterceptorAnnotationElement = interceptorAnnotationElement;
        this.mInterceptorClassElement = interceptorClassElement;
    }

    /**
     * Gets the fully qualified name of the interceptor class.
     * 
     * @return The fully qualified name of the interceptor class.
     */
    public String getQualifiedName() {

        return mInterceptorClassElement.getQualifiedName().toString();
    }

    /**
     * Gets the simple name of the interceptor class.
     * 
     * @return The simple name of the interceptor class.
     */
    public String getSimpleName() {

        return mInterceptorClassElement.getSimpleName().toString();
    }

    /**
     * Gets the annotation for this interceptor.
     * 
     * @return The annotation for this interceptor.
     */
    public TypeElement getInterceptorAnnotationElement() {
        return mInterceptorAnnotationElement;
    }
}
