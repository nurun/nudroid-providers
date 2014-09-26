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

import com.nudroid.annotation.processor.ProcessorUtils;
import com.nudroid.annotation.processor.UsedBy;
import com.nudroid.annotation.processor.ValidationErrorGatherer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

/**
 * An interceptorTypeElement for a delegate method.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Interceptor {

    private TypeElement interceptorTypeElement;
    private boolean hasCustomConstructor;
    //    private InterceptorPointAnnotationBlueprint concreteAnnotation;
    private List<InterceptorAnnotationParameter> concreteAnnotationConstructorArguments = new ArrayList<>();
    private String concreteAnnotationQualifiedName;

    private Interceptor() {

    }

    /**
     * Gets the fully qualified name of the interceptorTypeElement class.
     *
     * @return the fully qualified name of the interceptorTypeElement class
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getQualifiedName() {

        return interceptorTypeElement.getQualifiedName()
                .toString();
    }

    /**
     * Gets the simple name of the interceptorTypeElement class.
     *
     * @return the simple name of the interceptorTypeElement class
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getSimpleName() {

        return interceptorTypeElement.getSimpleName()
                .toString();
    }

    /**
     * Checks if the interceptorTypeElement has a custom (concrete annotation) constructor.
     *
     * @return <tt>true</tt> is it has, <tt>false</tt> otherwise
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean hasCustomConstructor() {

        return hasCustomConstructor;
    }

    /**
     * Gets the qualified name of the concrete annotation implementation for this interceptorTypeElement.
     *
     * @return the qualified name of the concrete annotation implementation for this interceptorTypeElement
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getConcreteAnnotationQualifiedName() {

        return concreteAnnotationQualifiedName;
    }

    /**
     * Gets the list of source code literals to create a new instance of the concrete annotation.
     *
     * @return the list of source code literals to create a new instance of the concrete annotation
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public List<InterceptorAnnotationParameter> getConcreteAnnotationConstructorArgumentLiterals() {

        return concreteAnnotationConstructorArguments;
    }

    /**
     * Adds an annotation constructor literal for the concrete annotation associated with this interceptorTypeElement to
     * the list of constructor arguments of the concrete annotation.
     *
     * @param value
     *         the literal to add
     */
    void addConcreteAnnotationConstructorLiteral(InterceptorAnnotationParameter value) {

        concreteAnnotationConstructorArguments.add(value);
    }

    /**
     * Builder for Interceptor.
     */
    public static class Builder implements ModelBuilder<Interceptor> {

        private final TypeElement interceptorTypeElement;
        private final InterceptorAnnotationBlueprints annotationBlueprints;

        /**
         * Initializes the builder.
         *
         * @param annotationBlueprints
         *         the blueprint for the annotation class binding the interceptorTypeElement point
         */
        public Builder(TypeElement interceptorTypeElement, InterceptorAnnotationBlueprints annotationBlueprints) {

            this.interceptorTypeElement = interceptorTypeElement;
            this.annotationBlueprints = annotationBlueprints;
        }

        /**
         * Builds an instance of the InterceptorPoint class.
         * <p>
         * {@inheritDoc}
         */
        public Interceptor build(ProcessorUtils processorUtils, Consumer<ValidationErrorGatherer> errorCallback) {

            Interceptor interceptor = new Interceptor();
            interceptor.interceptorTypeElement = this.interceptorTypeElement;
            interceptor.concreteAnnotationQualifiedName = this.annotationBlueprints.getConcreteClassSimpleName();

            List<ExecutableElement> constructors =
                    ElementFilter.constructorsIn(this.interceptorTypeElement.getEnclosedElements());

            for (ExecutableElement constructor : constructors) {

                final List<? extends VariableElement> parameters = constructor.getParameters();

                if (parameters.size() == 1 && processorUtils.isSame(parameters.get(0)
                        .asType(), this.annotationBlueprints.getTypeElement()
                        .asType())) {

                    interceptor.hasCustomConstructor = true;
                }
            }

            return interceptor;
        }
    }
}
