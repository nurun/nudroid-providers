package com.nudroid.annotation.processor.model;

import javax.lang.model.element.ExecutableElement;

/**
 * An attribute from a {@link ConcreteAnnotation}.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class AnnotationAttribute {

    private String mType;
    private String mName;
    private String mCapitalizedName;

    /**
     * TODO complete javadoc
     * 
     * @param method
     */
    public AnnotationAttribute(ExecutableElement method) {

        this.mType = method.getReturnType().toString();

        final String methodName = method.getSimpleName().toString();
        this.mName = methodName;
        this.mCapitalizedName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
    }

    /**
     * TODO complete javadoc
     * 
     * @return the type
     */
    public String getType() {
        return mType;
    }

    /**
     * TODO complete javadoc
     * 
     * @return the name
     */
    public String getName() {
        return mName;
    }

    /**
     * TODO complete javadoc
     * 
     * @return the capitalizedName
     */
    public String getCapitalizedName() {
        return mCapitalizedName;
    }
}
