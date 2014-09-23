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

import com.nudroid.annotation.processor.UsedBy;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * An interceptor for a delegate method.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class InterceptorPoint {

    private final TypeElement interceptorImplementationElement;
    private boolean hasCustomConstructor;
    private final InterceptorPointAnnotationBlueprint concreteAnnotation;
    private final List<InterceptorAnnotationParameter> concreteAnnotationConstructorArguments = new ArrayList<>();

    /**
     * Creates a new Interceptor bean.
     *
     * @param concreteAnnotation
     *         The {@link InterceptorPointAnnotationBlueprint} generated for this interceptor annotation type.
     */
    public InterceptorPoint(InterceptorPointAnnotationBlueprint concreteAnnotation) {

        this.interceptorImplementationElement = concreteAnnotation.getInterceptorTypeElement();
        this.concreteAnnotation = concreteAnnotation;
    }

    /**
     * Gets the fully qualified name of the interceptor class.
     *
     * @return The fully qualified name of the interceptor class.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getQualifiedName() {

        return interceptorImplementationElement.getQualifiedName()
                .toString();
    }

    /**
     * Gets the simple name of the interceptor class.
     *
     * @return The simple name of the interceptor class.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getSimpleName() {

        return interceptorImplementationElement.getSimpleName()
                .toString();
    }

    /**
     * Checks if the interceptor has a custom (concrete annotation) constructor.
     *
     * @return <tt>true</tt> is it has, <tt>false</tt> otherwise.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean hasCustomConstructor() {

        return hasCustomConstructor;
    }

    /**
     * Gets the qualified name of the concrete annotation implementation for this interceptor.
     *
     * @return the qualified name of the concrete annotation implementation for this interceptor.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getConcreteAnnotationQualifiedName() {

        return concreteAnnotation.getConcreteClassName();
    }

    /**
     * Gets the list of source code literals to create a new instance of the concrete annotation.
     *
     * @return The list of source code literals to create a new instance of the concrete annotation.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public List<InterceptorAnnotationParameter> getConcreteAnnotationConstructorArgumentLiterals() {

        return concreteAnnotationConstructorArguments;
    }

    /**
     * Adds an annotation constructor literal for the concrete annotation associated with this interceptor to the list
     * of constructor arguments of the concrete annotation.
     *
     * @param value
     *         The literal to add.
     */
    void addConcreteAnnotationConstructorLiteral(InterceptorAnnotationParameter value) {

        concreteAnnotationConstructorArguments.add(value);
    }

    /**
     * Sets if this Interceptor have a custom constructor.
     *
     * @param hasCustomConstructor
     *         <tt>true</tt> if it has, <tt>false</tt> otherwise.
     */
    void setHasCustomConstructor(boolean hasCustomConstructor) {

        this.hasCustomConstructor = hasCustomConstructor;
    }
}
