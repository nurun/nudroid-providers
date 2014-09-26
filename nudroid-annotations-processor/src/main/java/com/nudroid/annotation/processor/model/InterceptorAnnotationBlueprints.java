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

import com.nudroid.annotation.processor.LoggingUtils;
import com.nudroid.annotation.processor.ProcessorUtils;
import com.nudroid.annotation.processor.UsedBy;
import com.nudroid.annotation.processor.ValidationErrorGatherer;
import com.nudroid.provider.interceptor.ContentProviderInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Represents a concrete annotation implementation that will be created by the source code generator.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class InterceptorAnnotationBlueprints {

    private String interceptorAnnotationQualifiedName;
    private TypeElement interceptorAnnotationTypeElement;
    private TypeElement interceptorImplementationTypeElement;
    private List<AnnotationElement> attributes = new ArrayList<>();
    private String qualifiedClassName;

    private InterceptorAnnotationBlueprints() {

    }

    /**
     * Adds an attribute to this annotation representation.
     *
     * @param attribute
     *         The attribute to add.
     */
    public void addAttribute(AnnotationElement attribute) {

        this.attributes.add(attribute);
    }

    /**
     * Gets the attributes of this concrete annotation.
     *
     * @return The attributes of this concrete annotation.
     */
    @UsedBy({"ConcreteAnnotationTemplate.stg"})
    public List<AnnotationElement> getAttributes() {

        return attributes;
    }

    /**
     * Gets the {@link TypeElement} of the annotation.
     *
     * @return The {@link TypeElement} of the annotation.
     */
    public TypeElement getTypeElement() {

        return interceptorAnnotationTypeElement;
    }

    /**
     * Gets the interceptor's {@link TypeElement} this annotation is for.
     *
     * @return The {@link TypeElement} of the interceptor implementation.
     */
    public TypeElement getInterceptorTypeElement() {

        return interceptorImplementationTypeElement;
    }

    /**
     * Gets the qualified name of the annotation class.
     *
     * @return The qualified name of the annotation class.
     */
    public String getAnnotationQualifiedName() {
        return interceptorAnnotationQualifiedName;
    }

    /**
     * Gets the name of the concrete annotation implementation class.
     *
     * @return The name of the concrete annotation implementation class.
     */
    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    /**
     * Creates the {@link Interceptor}.
     *
     * @param annotationMirror
     *         the {@link AnnotationMirror} for the interceptor annotation
     * @param processorUtils
     *         a {@link com.nudroid.annotation.processor.ProcessorUtils} instance
     * @param logger
     *         a {@link LoggingUtils} instance
     *
     * @return a new interceptor point description
     */
    public Interceptor createInterceptor(AnnotationMirror annotationMirror, ProcessorUtils processorUtils,
                                         LoggingUtils logger) {

        Interceptor interceptor =
                new Interceptor.Builder(interceptorImplementationTypeElement, this).build(processorUtils,
                        gatherer -> gatherer.logErrors(logger));

        SortedSet<ExecutableElement> sortedAnnotationElements = new TreeSet<>(
                (ExecutableElement o1, ExecutableElement o2) -> o1.getSimpleName()
                        .toString()
                        .compareTo(o2.getSimpleName()
                                .toString()));

        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues =
                processorUtils.getAnnotationValuesWithDefaults(annotationMirror);
        sortedAnnotationElements.addAll(annotationValues.keySet());

        for (ExecutableElement annotationElement : sortedAnnotationElements) {

            AnnotationValue elementValue = annotationValues.get(annotationElement);

            InterceptorAnnotationParameter annotationElementValue =
                    new InterceptorAnnotationParameter.Builder(annotationElement, elementValue).build(processorUtils,
                            gatherer -> gatherer.logErrors(logger));
            interceptor.addConcreteAnnotationConstructorLiteral(annotationElementValue);
        }

        return interceptor;
    }

    /**
     * Builder for Interceptor.
     */
    public static class Builder implements ModelBuilder<InterceptorAnnotationBlueprints> {

        private final TypeElement annotationTypeElement;

        /**
         * Initializes the builder.
         *
         * @param annotationTypeElement
         *         the type element for the annotation
         */
        public Builder(TypeElement annotationTypeElement) {

            this.annotationTypeElement = annotationTypeElement;
        }

        /**
         * Builds an instance of the InterceptorPoint class.
         * <p>
         * {@inheritDoc}
         */
        public InterceptorAnnotationBlueprints build(ProcessorUtils processorUtils,
                                                     Consumer<ValidationErrorGatherer> errorCallback) {

            ValidationErrorGatherer gatherer = new ValidationErrorGatherer();
            validateInterceptorClass(processorUtils, gatherer);

            InterceptorAnnotationBlueprints blueprints = new InterceptorAnnotationBlueprints();
            blueprints.interceptorAnnotationTypeElement = this.annotationTypeElement;
            blueprints.interceptorImplementationTypeElement =
                    (TypeElement) this.annotationTypeElement.getEnclosingElement();
            blueprints.interceptorAnnotationQualifiedName = this.annotationTypeElement.getQualifiedName()
                    .toString();

            blueprints.qualifiedClassName =
                    processorUtils.generateCompositeElementName(this.annotationTypeElement) + "$ConcreteAnnotation_";

            if (gatherer.hasErrors()) {

                errorCallback.accept(gatherer);
                return null;
            } else {

                return blueprints;
            }
        }

        private void validateInterceptorClass(ProcessorUtils processorUtils, ValidationErrorGatherer gatherer) {

            Element interceptorClass = this.annotationTypeElement.getEnclosingElement();

            /* Interceptor annotations must be inner classes of the actual interceptor implementation. */
            if (!processorUtils.isClass(interceptorClass)) {

                gatherer.gatherError(String.format(
                                "Interceptor annotations must be static elements of an enclosing %s implementation",
                                ContentProviderInterceptor.class.getName()), this.annotationTypeElement,
                        LoggingUtils.LogLevel.ERROR);
            } else if (!processorUtils.implementsInterface((TypeElement) interceptorClass,
                    ContentProviderInterceptor.class)) {

                gatherer.gatherError(String.format("Interceptor class %s must implement interface %s", interceptorClass,
                                ContentProviderInterceptor.class.getName()), interceptorClass,
                        LoggingUtils.LogLevel.ERROR);
            }
        }
    }
}