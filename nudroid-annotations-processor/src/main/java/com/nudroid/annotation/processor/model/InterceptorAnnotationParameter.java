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

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * A value from an interceptor annotation element. Given the following interceptor applied to a delegate method:
 * <pre>
 *     &#64;SampleInterceptor(aValue = "Hello")
 *     public Cursor aQuery {
 *         String anElement() default "";
 *     }
 * </pre>
 * The InterceptorAnnotationParameter denotes the value "Hello".
 */
public class InterceptorAnnotationParameter {

    private String literalValue;
    private boolean isString;

    private InterceptorAnnotationParameter() {
    }

    /**
     * Gets the literal representation of this parameter (as it appears in the source code).
     *
     * @return The literal representation of this parameter (as it appears in the source code).
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getLiteralValue() {
        return literalValue;
    }

    /**
     * Checks if this parameter is a String or an array of Strings.
     *
     * @return <tt>true</tt> if this parameter is a String or array of Strings, <tt>false</tt> otherwise
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isString() {

        return isString;
    }

    /**
     * Builder for a DelegateMethod.
     */
    public static class Builder implements ModelBuilder<InterceptorAnnotationParameter> {

        private final ExecutableElement annotationElement;
        private final AnnotationValue annotationElementValue;

        /**
         * Initializes the builder.
         *
         * @param annotationElement
         *         the Java Element denoting the annotation element
         * @param annotationElementValue
         *         the value of the annotation element
         */
        public Builder(ExecutableElement annotationElement, AnnotationValue annotationElementValue) {

            this.annotationElement = annotationElement;
            this.annotationElementValue = annotationElementValue;
        }

        /**
         * Creates the InterceptorAnnotationParameter instance.
         * <p>
         * {@inheritDoc}
         */
        public InterceptorAnnotationParameter build(ProcessorUtils processorUtils,
                                                    Consumer<ValidationErrorGatherer> errorCallback) {

            ValidationErrorGatherer gatherer = new ValidationErrorGatherer();

            final TypeKind kind = this.annotationElement.getReturnType()
                    .getKind();

            InterceptorAnnotationParameter parameter = new InterceptorAnnotationParameter();

            switch (kind) {
                case ARRAY:

                    return generateAnnotationArrayLiteral(processorUtils, errorCallback, parameter, gatherer);
                case BOOLEAN:

                    parameter.literalValue = String.format("%s", annotationElementValue.getValue());
                    break;
                case CHAR:

                    parameter.literalValue = String.format("'%s'", annotationElementValue.getValue());
                    break;
                case FLOAT:

                    parameter.literalValue = String.format("%sf", annotationElementValue.getValue());
                    break;
                case DOUBLE:

                    parameter.literalValue = String.format("%s", annotationElementValue.getValue());
                    break;
                case BYTE:

                    parameter.literalValue = String.format("(byte) %s", annotationElementValue.getValue());
                    break;
                case INT:

                    parameter.literalValue = String.format("%s", annotationElementValue.getValue());
                    break;
                case LONG:

                    parameter.literalValue = String.format("%sL", annotationElementValue.getValue());
                    break;
                case DECLARED:

                    TypeMirror annotationElementValueType = this.annotationElement.getReturnType();

                    if (processorUtils.isString(annotationElementValueType)) {

                        parameter.literalValue = String.format("\"%s\"", annotationElementValue.getValue());
                        parameter.isString = true;
                    } else if (processorUtils.isClass(annotationElementValueType)) {

                        parameter.literalValue = String.format("%s.class", annotationElementValue.getValue());
                    } else {

                        if (processorUtils.isEnum(annotationElementValueType)) {

                            parameter.literalValue = String.format(String.format("%s.%s", annotationElementValueType,
                                    this.annotationElementValue.getValue()));
                        } else {

                            gatherer.gatherError(String.format(
                                            "Invalid type %s for the annotation attribute %s; only primitive types, Strings, " +
                                                    "Class, and enumerations are permitted or 1-dimensional arrays thereof.",
                                            this.annotationElement.getReturnType(), this.annotationElement),
                                    annotationElement, LoggingUtils.LogLevel.ERROR);
                        }
                    }

                    break;

                default:
                    gatherer.gatherError(String.format(
                                    "Invalid type %s for the annotation attribute %s; only primitive types, Strings, " +
                                            "Class, and enumerations are permitted or 1-dimensional arrays thereof.",
                                    this.annotationElement.getReturnType(), this.annotationElement), annotationElement,
                            LoggingUtils.LogLevel.ERROR);
            }

            if (gatherer.hasErrors()) {

                errorCallback.accept(gatherer);
                return null;
            } else {

                return parameter;
            }
        }

        private InterceptorAnnotationParameter generateAnnotationArrayLiteral(ProcessorUtils processorUtils,
                                                                              Consumer<ValidationErrorGatherer> errorCallback,
                                                                              InterceptorAnnotationParameter parameter,
                                                                              ValidationErrorGatherer gatherer) {
            ArrayType arrayType = (ArrayType) this.annotationElement.getReturnType();

            @SuppressWarnings("unchecked") List<Object> arrayElements =
                    ((List<? extends AnnotationValue>) this.annotationElementValue.getValue()).stream()
                            .map(AnnotationValue::getValue)
                            .collect(Collectors.toList());

            StringBuilder arrayInitializer = new StringBuilder();

            switch (arrayType.getComponentType()
                    .getKind()) {
                case BOOLEAN:

                    arrayInitializer.append("new boolean[] { ");

                    String elements = arrayElements.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));

                    arrayInitializer.append(elements);

                    arrayInitializer.append(" }");
                    break;
                case CHAR:

                    arrayInitializer.append("new char[] { '");

                    elements = arrayElements.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("', '"));

                    arrayInitializer.append(elements);

                    arrayInitializer.append("' }");
                    break;
                case FLOAT:

                    arrayInitializer.append("new float[] { ");

                    elements = arrayElements.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("f, "));

                    arrayInitializer.append(elements);

                    arrayInitializer.append("f }");
                    break;
                case DOUBLE:

                    arrayInitializer.append("new double[] { ");

                    elements = arrayElements.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));

                    arrayInitializer.append(elements);

                    arrayInitializer.append(" }");
                    break;
                case BYTE:

                    arrayInitializer.append("new byte[] { ");

                    elements = arrayElements.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));

                    arrayInitializer.append(elements);

                    arrayInitializer.append(" }");
                    break;
                case INT:

                    arrayInitializer.append("new int[] { ");

                    elements = arrayElements.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));

                    arrayInitializer.append(elements);

                    arrayInitializer.append(" }");
                    break;
                case LONG:

                    arrayInitializer.append("new long[] { ");

                    elements = arrayElements.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("L, "));

                    arrayInitializer.append(elements);

                    arrayInitializer.append("L }");
                    break;

                case DECLARED:

                    if (processorUtils.isString(arrayType.getComponentType())) {

                        arrayInitializer.append("new String[] { \"");

                        elements = arrayElements.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining("\", \""));

                        arrayInitializer.append(elements);

                        arrayInitializer.append("\" }");
                    } else if (processorUtils.isClass(arrayType.getComponentType())) {

                        arrayInitializer.append("new Class[] { ");

                        elements = arrayElements.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(".class, "));

                        arrayInitializer.append(elements);

                        arrayInitializer.append(".class }");
                    } else {

                        if (processorUtils.isEnum(arrayType.getComponentType())) {

                            arrayInitializer.append(String.format("new %s[] { %s.", arrayType.getComponentType(),
                                    arrayType.getComponentType()));

                            elements = arrayElements.stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(String.format(", %s.", arrayType.getComponentType())));

                            arrayInitializer.append(elements);

                            arrayInitializer.append(" }");
                        } else {

                            gatherer.gatherError(String.format("Invalid type %s for the annotation attribute " +
                                            "%s; only primitive types, Strings, Class and enumerations are permitted or " +
                                            "1-dimensional arrays" +
                                            " thereof.", arrayType.getComponentType(), this.annotationElement),
                                    this.annotationElement, LoggingUtils.LogLevel.ERROR);
                        }
                    }

                    break;
                default:
                    System.out.println("Woa not found");
                    break;
            }

            parameter.literalValue = arrayInitializer.toString();

            if (gatherer.hasErrors()) {

                errorCallback.accept(gatherer);
                return null;
            } else {

                return parameter;
            }
        }
    }
}
