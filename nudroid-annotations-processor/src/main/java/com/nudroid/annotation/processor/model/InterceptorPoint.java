package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * An interceptor for a delegate method.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class InterceptorPoint {

    private TypeElement mInterceptorImplementationElement;
    private TypeElement mInterceptorAnnotationElement;
    private boolean mHasCustomConstructor;
    private boolean mHasDefaultConstructor;
    private InterceptorAnnotationBlueprint mConcreteAnnotation;
    private List<InterceptorAnnotationParameter> mConcreteAnnotationConstructorArguments = new ArrayList<InterceptorAnnotationParameter>();

    /**
     * Creates a new Interceptor bean.
     * 
     * @param concreteAnnotation
     *            The {@link InterceptorAnnotationBlueprint} generated for this interceptor annotation type.
     */
    public InterceptorPoint(InterceptorAnnotationBlueprint concreteAnnotation) {

        this.mInterceptorAnnotationElement = concreteAnnotation.getTypeElement();
        this.mInterceptorImplementationElement = concreteAnnotation.getInterceptorTypeElement();
        this.mConcreteAnnotation = concreteAnnotation;
    }

    /**
     * Gets the fully qualified name of the interceptor class.
     * 
     * @return The fully qualified name of the interceptor class.
     */
    public String getQualifiedName() {

        return mInterceptorImplementationElement.getQualifiedName().toString();
    }

    /**
     * Gets the simple name of the interceptor class.
     * 
     * @return The simple name of the interceptor class.
     */
    public String getSimpleName() {

        return mInterceptorImplementationElement.getSimpleName().toString();
    }

    /**
     * Gets the annotation for this interceptor.
     * 
     * @return The annotation for this interceptor.
     */
    public TypeElement getInterceptorAnnotationElement() {
        return mInterceptorAnnotationElement;
    }

    /**
     * Checks if the interceptor has a default constructor.
     * 
     * @return <tt>true</tt> is it has, <tt>false</tt> otherwise.
     */
    public boolean hasDefaultConstructor() {

        return mHasDefaultConstructor;
    }

    /**
     * Checks if the interceptor has a custom (concrete annotation) constructor.
     * 
     * @return <tt>true</tt> is it has, <tt>false</tt> otherwise.
     */
    public boolean hasCustomConstructor() {

        return mHasCustomConstructor;
    }

    /**
     * Gets the qualified name of the concrete annotation implementation for this interceptor.
     * 
     * @return the qualified name of the concrete annotation implementation for this interceptor.
     */
    public String getConcreteAnnotationQualifiedName() {

        return mConcreteAnnotation.getConcreteClassName();
    }

    /**
     * Gets the list of source code literals to create a new instance of the concrete annotation.
     * 
     * @return The list of source code literals to create a new instance of the concrete annotation.
     */
    public List<InterceptorAnnotationParameter> getConcreteAnnotationConstructorArgumentLiterals() {

        return mConcreteAnnotationConstructorArguments;
    }

    /**
     * Adds an annotation constructor literal for the concrete annotation associated with this interceptor to the list
     * of constructor arguments of the concrete annotation.
     * 
     * @param value
     *            The literal to add.
     */
    void addConcreteAnnotationConstructorLiteral(InterceptorAnnotationParameter value) {
    
        mConcreteAnnotationConstructorArguments.add(value);
    }

    /**
     * Sets if this Interceptor have a default constructor.
     * 
     * @param <tt>true</tt> if it has, <tt>false</tt> otherwise.
     */
    void setHasDefaultConstructor(boolean hasDefaultConstructor) {

        this.mHasDefaultConstructor = hasDefaultConstructor;
    }

    /**
     * Sets if this Interceptor have a custom constructor.
     * 
     * @param <tt>true</tt> if it has, <tt>false</tt> otherwise.
     */
    void setHasCustomConstructor(boolean hasCustomConstructor) {

        this.mHasCustomConstructor = hasCustomConstructor;
    }
}
