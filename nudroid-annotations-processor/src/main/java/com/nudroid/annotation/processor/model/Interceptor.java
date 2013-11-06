package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

/**
 * Information about delegate method interceptors.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Interceptor {

    private TypeElement mInterceptorClassElement;
    private TypeElement mInterceptorAnnotationElement;
    private boolean mHasCustomConstructor;
    private boolean mHasDefaultConstrucotr;
    private ConcreteAnnotation mConcreteAnnotation;
    private List<String> mConcreteAnnotationConstructorArguments = new ArrayList<String>();

    /**
     * Creates a new Interceptor bean.
     * @param concreteAnnotation
     *            The {@link ConcreteAnnotation} generated for this interceptor annotation type.
     * @param interceptorAnnotationElement
     *            The {@link TypeElement} for the annotation for this interceptor.
     * @param interceptorClassElement
     *            The {@link TypeElement} for the concrete implementation for this interceptor.
     */
    public Interceptor(ConcreteAnnotation concreteAnnotation) {

        this.mInterceptorAnnotationElement = concreteAnnotation.getTypeElement();
        this.mInterceptorClassElement = concreteAnnotation.getInterceptorTypeElement();
        this.mConcreteAnnotation = concreteAnnotation;

        List<ExecutableElement> constructors = ElementFilter.constructorsIn(mInterceptorClassElement
                .getEnclosedElements());

        for (ExecutableElement constructor : constructors) {

            final List<? extends VariableElement> parameters = constructor.getParameters();

            if (parameters.size() == 0) {
                this.mHasDefaultConstrucotr = true;
            }

            if (parameters.size() == 1
                    && parameters.get(0).asType().toString().equals(mInterceptorAnnotationElement.asType())) {

                this.mHasCustomConstructor = true;
            }
        }
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

    /**
     * Checks if the interceptor has a default constructor.
     * 
     * @return <tt>true</tt> is it has, <tt>false</tt> otherwise.
     */
    public boolean hasDefaultConstructor() {

        return mHasDefaultConstrucotr;
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
     * Adds an annotation constructor literal for the concrete annotation associated with this interceptor to the list
     * of constructor arguments of the concrete annotation.
     * 
     * @param value
     *            The literal to add.
     */
    public void addConcreteAnnotationConstructorLiteral(String value) {

        mConcreteAnnotationConstructorArguments.add(value);
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
    public List<String> getConcreteAnnotationConstructorArgumentLiterals() {

        return mConcreteAnnotationConstructorArguments;
    }
}
