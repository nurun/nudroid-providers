package com.nudroid.annotation.processor.model;

import javax.lang.model.element.ExecutableElement;

/**
 * An annotation attribute (method) member of a {@link InterceptorAnnotationBlueprint}.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class AnnotationAttribute {

    private String mType;
    private String mName;
    private String mCapitalizedName;

    /**
     * Creates an instance of this class.
     * 
     * @param method
     *            The {@link ExecutableElement} for the attribute (method) of the annotation interceptor.
     */
    public AnnotationAttribute(ExecutableElement method) {

        this.mType = method.getReturnType().toString();

        final String methodName = method.getSimpleName().toString();
        this.mName = methodName;
        this.mCapitalizedName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
    }

    /**
     * Gets the (return) type of this attribute.
     * 
     * @return The type of this attribute.
     */
    public String getType() {

        return mType;
    }

    /**
     * Gets the name of this attribute.
     * 
     * @return The name of this attribute.
     */
    public String getName() {

        return mName;
    }

    /**
     * Gets the capitalized name of this attribute.
     * 
     * @return The capitalized name of this attribute.
     */
    public String getCapitalizedName() {

        return mCapitalizedName;
    }
}
