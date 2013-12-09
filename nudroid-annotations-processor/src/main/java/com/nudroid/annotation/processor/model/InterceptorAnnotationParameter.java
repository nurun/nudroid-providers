package com.nudroid.annotation.processor.model;

/**
 * Represents an interceptor annotation parameter.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class InterceptorAnnotationParameter {

    private String mLiteralValue;
    private Class<?> mType;
    private boolean mIsString;

    /**
     * Creates an interceptor annotation parameter.
     * 
     * @param literalValue
     *            The literal value for this parameter, as it appears in the source code.
     * @param parameterType
     *            The type of this parameter.
     */
    public InterceptorAnnotationParameter(String literalValue, Class<?> parameterType) {

        this.mLiteralValue = literalValue;
        this.mType = parameterType;

        System.out.println("*********" + mType);
        mIsString = mType.equals(String.class) || mType.equals(String[].class);
        System.out.println("*********" + mIsString);
    }

    /**
     * Gets the literal representation of this parameter (as it appears in the source code).
     * 
     * @return The literal representation of this parameter (as it appears in the source code).
     */
    public String getLiteralValue() {
        return mLiteralValue;
    }

    /**
     * Gets the parameter type.
     * 
     * @return The parameter type.
     */
    public Class<?> getParameterType() {
        return mType;
    }

    /**
     * Checks if this parameter is a String or an array of Strings.
     * 
     * @return <tt>true</tt> if this parameter is a String or array of Strings, <tt>false</tt> otherwise
     */
    public boolean isString() {

        return mIsString;
    }
}
