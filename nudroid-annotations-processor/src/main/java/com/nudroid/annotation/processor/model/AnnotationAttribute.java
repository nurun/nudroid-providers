/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
