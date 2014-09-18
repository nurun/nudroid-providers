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

import com.google.common.base.Joiner;
import com.nudroid.annotation.processor.LoggingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Represents a concrete annotation implementation that will be created by the source code generator.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class InterceptorAnnotationBlueprint {

    private String mInterceptorAnnotationQualifiedName;
    private String mConcreteClassName;
    private String mConcretePackageName;
    private TypeElement mInterceptorAnnotationTypeElement;
    private TypeElement mInterceptorImplementationTypeElement;
    private boolean mHasCustomConstructor;
    private List<AnnotationAttribute> mAttributes = new ArrayList<AnnotationAttribute>();

    /**
     * Creates a new InterceptorBlueprint bean.
     *
     * @param typeElement
     *         The {@link TypeElement} for the annotation this class represents.
     */
    public InterceptorAnnotationBlueprint(TypeElement typeElement) {

        this.mInterceptorAnnotationTypeElement = typeElement;
        this.mInterceptorImplementationTypeElement = (TypeElement) typeElement.getEnclosingElement();
        this.mInterceptorAnnotationQualifiedName = typeElement.getQualifiedName()
                .toString();

        List<ExecutableElement> constructors =
                ElementFilter.constructorsIn(mInterceptorImplementationTypeElement.getEnclosedElements());

        for (ExecutableElement constructor : constructors) {

            final List<? extends VariableElement> parameters = constructor.getParameters();

            // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same qualified
            // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
            if (parameters.size() == 1 && parameters.get(0)
                    .asType()
                    .toString()
                    .equals(mInterceptorAnnotationTypeElement.asType()
                            .toString())) {

                this.mHasCustomConstructor = true;
            }
        }

        calculateConcreteClassAndPackageNames(typeElement);
    }

    /**
     * Adds an attribute to this annotation representation.
     *
     * @param attribute
     *         The attribute to add.
     */
    public void addAttribute(AnnotationAttribute attribute) {

        this.mAttributes.add(attribute);
    }

    /**
     * Gets the attributes of this concrete annotation.
     *
     * @return The attributes of this concrete annotation.
     */
    public List<AnnotationAttribute> getAttributes() {

        return mAttributes;
    }

    /**
     * Gets the {@link TypeElement} of the annotation.
     *
     * @return The {@link TypeElement} of the annotation.
     */
    public TypeElement getTypeElement() {

        return mInterceptorAnnotationTypeElement;
    }

    /**
     * Gets the interceptor's {@link TypeElement} this annotation is for.
     *
     * @return The {@link TypeElement} of the interceptor implementation.
     */
    public TypeElement getInterceptorTypeElement() {

        return mInterceptorImplementationTypeElement;
    }

    /**
     * Gets the qualified name of the annotation class.
     *
     * @return The qualified name of the annotation class.
     */
    public String getAnnotationQualifiedName() {
        return mInterceptorAnnotationQualifiedName;
    }

    /**
     * Gets the name of the concrete annotation implementation class.
     *
     * @return The name of the concrete annotation implementation class.
     */
    public String getConcreteClassName() {
        return mConcreteClassName;
    }

    /**
     * Gets the package name of the concrete annotation implementation class.
     *
     * @return The package name of the concrete annotation implementation class.
     */
    public String getConcretePackageName() {
        return mConcretePackageName;
    }

    /**
     * Creates the {@link InterceptorPoint}.
     *
     * @param annotationMirror
     *         The {@link AnnotationMirror} for the interceptor annotation.
     * @param elementUtils
     *         An {@link Elements} instance.
     * @param typeUtils
     *         An {@link Types} instance.
     * @param logger
     *         A {@link LoggingUtils} instance.
     *
     * @return A new interceptor point description.
     */
    public InterceptorPoint createInterceptorPoint(AnnotationMirror annotationMirror, Elements elementUtils,
                                                   Types typeUtils, LoggingUtils logger) {

        InterceptorPoint interceptor = new InterceptorPoint(this);
        interceptor.setHasCustomConstructor(mHasCustomConstructor);

        SortedSet<ExecutableElement> methodKeys = new TreeSet<>(
                (ExecutableElement o1, ExecutableElement o2) -> o1.getSimpleName()
                        .toString()
                        .compareTo(o2.getSimpleName()
                                .toString()));

        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues =
                elementUtils.getElementValuesWithDefaults(annotationMirror);
        methodKeys.addAll(annotationValues.keySet());

        for (ExecutableElement keyEntry : methodKeys) {

            AnnotationValue attributeValue = annotationValues.get(keyEntry);

            generateAnnotationLiteral(interceptor, keyEntry, attributeValue, elementUtils, typeUtils, logger);
        }

        return interceptor;
    }

    private void calculateConcreteClassAndPackageNames(TypeElement typeElement) {

        Element parentElement = typeElement.getEnclosingElement();
        StringBuilder concreteSimpleName = new StringBuilder(typeElement.getSimpleName());

        while (parentElement != null && !parentElement.getKind()
                .equals(ElementKind.PACKAGE)) {

            concreteSimpleName.insert(0, "$")
                    .insert(0, parentElement.getSimpleName());
            parentElement = parentElement.getEnclosingElement();
        }

        concreteSimpleName.insert(0, "Concrete");

        if (parentElement != null && parentElement.getKind()
                .equals(ElementKind.PACKAGE)) {

            this.mConcretePackageName = ((PackageElement) parentElement).getQualifiedName()
                    .toString();
        } else {

            this.mConcretePackageName = "";
        }

        this.mConcreteClassName = concreteSimpleName.toString();
    }

    private void generateAnnotationLiteral(InterceptorPoint interceptor, ExecutableElement attribute,
                                           AnnotationValue attributeValue, Elements elementUtils, Types typeUtils,
                                           LoggingUtils logger) {
        final TypeKind kind = attribute.getReturnType()
                .getKind();

        switch (kind) {
            case ARRAY:

                generateAnnotationArrayLiteral(interceptor, attribute, attributeValue, elementUtils, typeUtils, logger);

                break;
            case CHAR:

                interceptor.addConcreteAnnotationConstructorLiteral(
                        new InterceptorAnnotationParameter(String.format("'%s'", attributeValue.getValue()),
                                char.class));
                break;
            case FLOAT:

                interceptor.addConcreteAnnotationConstructorLiteral(
                        new InterceptorAnnotationParameter(String.format("%sf", attributeValue.getValue()),
                                float.class));
                break;
            case DOUBLE:

                interceptor.addConcreteAnnotationConstructorLiteral(
                        new InterceptorAnnotationParameter(String.format("%s", attributeValue.getValue()),
                                double.class));
                break;
            case BYTE:

                interceptor.addConcreteAnnotationConstructorLiteral(
                        new InterceptorAnnotationParameter(String.format("(byte) %s", attributeValue.getValue()),
                                long.class));
                break;

            case INT:

                interceptor.addConcreteAnnotationConstructorLiteral(
                        new InterceptorAnnotationParameter(String.format("%s", attributeValue.getValue()),
                                long.class));
                break;
            case LONG:

                interceptor.addConcreteAnnotationConstructorLiteral(
                        new InterceptorAnnotationParameter(String.format("%sL", attributeValue.getValue()), int.class));
                break;

            case DECLARED:

                TypeElement stringType = elementUtils.getTypeElement(String.class.getName());

                // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same qualified
                // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
                if (typeUtils.isSameType(stringType.asType(), attribute.getReturnType())) {

                    interceptor.addConcreteAnnotationConstructorLiteral(
                            new InterceptorAnnotationParameter(String.format("\"%s\"", attributeValue.getValue()),
                                    String.class));
                } else if (Class.class.getName()
                        .equals(typeUtils.asElement(attribute.getReturnType())
                                .toString())) {

                    interceptor.addConcreteAnnotationConstructorLiteral(
                            new InterceptorAnnotationParameter(String.format("%s.class", attributeValue.getValue()),
                                    Class.class));
                } else {

                    final Element asElement = typeUtils.asElement(attribute.getReturnType());

                    if (asElement.getKind() == ElementKind.ENUM) {
                        interceptor.addConcreteAnnotationConstructorLiteral(new InterceptorAnnotationParameter(
                                String.format("%s.%s", asElement, attributeValue.getValue()), Object.class));
                    } else {

                        logger.error(String.format("Invalid type %s for the annotation attribute " +
                                "%s; only primitive type, String and enumeration are permitted or 1-dimensional arrays" +
                                " thereof.", asElement, attribute), attribute);
                    }
                }

                break;

            default:
                interceptor.addConcreteAnnotationConstructorLiteral(
                        new InterceptorAnnotationParameter(String.format("%s", attributeValue.getValue()),
                                Object.class));
        }
    }

    private void generateAnnotationArrayLiteral(InterceptorPoint interceptor, ExecutableElement attribute,
                                                AnnotationValue attributeValue, Elements elementUtils, Types typeUtils,
                                                LoggingUtils logger) {

        ArrayType arrayType = (ArrayType) attribute.getReturnType();
        Class<?> arrayClass = null;

        @SuppressWarnings("unchecked") List<? extends AnnotationValue> annotationValues =
                (List<? extends AnnotationValue>) attributeValue.getValue();

        List<Object> arrayElements = annotationValues.stream()
                .map(AnnotationValue::getValue)
                .collect(Collectors.toList());

        StringBuilder arrayInitializer = new StringBuilder();

        /*
         * On Eclipse, the toString() method of the AnnotationValues of an array is not proper. It does not print the
         * fully qualified name of enum, does not surround characters and strings wiith ' and " and does not append 'f'
         * or 'l' for floats or longs. Thus the need to recreate the logic for proper source code generation.
         */
        switch (arrayType.getComponentType()
                .getKind()) {
            case CHAR:

                arrayInitializer.append("new char[] { '");

                String elements = arrayElements.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("', '"));

                arrayInitializer.append(elements);

                arrayInitializer.append("' }");
                arrayClass = char[].class;
                break;
            case FLOAT:

                arrayInitializer.append("new float[] { ");
                Joiner.on("f, ")
                        .skipNulls()
                        .appendTo(arrayInitializer, arrayElements);
                arrayInitializer.append("f }");
                arrayClass = float[].class;
                break;
            case DOUBLE:

                arrayInitializer.append("new double[] { ");
                Joiner.on(", ")
                        .skipNulls()
                        .appendTo(arrayInitializer, arrayElements);
                arrayInitializer.append(" }");
                arrayClass = double[].class;
                break;
            case BYTE:

                arrayInitializer.append("new byte[] { ");
                Joiner.on(", ")
                        .skipNulls()
                        .appendTo(arrayInitializer, arrayElements);
                arrayInitializer.append(" }");
                arrayClass = byte[].class;
                break;
            case INT:

                arrayInitializer.append("new int[] { ");
                Joiner.on(", ")
                        .skipNulls()
                        .appendTo(arrayInitializer, arrayElements);
                arrayInitializer.append(" }");
                arrayClass = int[].class;
                break;
            case LONG:

                arrayInitializer.append("new long[] { ");
                Joiner.on("L, ")
                        .skipNulls()
                        .appendTo(arrayInitializer, arrayElements);
                arrayInitializer.append("L }");
                arrayClass = long[].class;
                break;

            case DECLARED:

                TypeElement stringType = elementUtils.getTypeElement(String.class.getName());
                TypeElement classType = elementUtils.getTypeElement(Class.class.getName());

                // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same qualified
                // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
                if (typeUtils.isSameType(stringType.asType(), arrayType.getComponentType())) {

                    arrayInitializer.append("new String[] { \"");
                    Joiner.on("\", \"")
                            .skipNulls()
                            .appendTo(arrayInitializer, arrayElements);
                    arrayInitializer.append("\" }");
                    arrayClass = String[].class;
                } else if (Class.class.getName()
                        .equals(typeUtils.asElement(arrayType.getComponentType())
                                .toString())) {

                    arrayInitializer.append("new Class[] { ");
                    Joiner.on(".class, ")
                            .skipNulls()
                            .appendTo(arrayInitializer, arrayElements);
                    arrayInitializer.append(".class }");
                    arrayClass = Class[].class;
                } else {

                    final Element arrayElementType = typeUtils.asElement(arrayType.getComponentType());

                    if (arrayElementType.getKind() == ElementKind.ENUM) {

                        arrayInitializer.append(String.format("new %s[] { %s.", arrayElementType, arrayElementType));
                        Joiner.on(String.format(", %s.", arrayElementType))
                                .skipNulls()
                                .appendTo(arrayInitializer, arrayElements);
                        arrayInitializer.append(" }");
                        arrayClass = Object[].class;
                    } else {

                        logger.error(String.format("Invalid type %s for the annotation attribute " +
                                "%s; only primitive type, String and enumeration are permitted or 1-dimensional arrays" +
                                " thereof.", arrayElementType, attribute), attribute);
                    }
                }

                break;
            default:
                System.out.println("Woa not found");
                break;
        }

        interceptor.addConcreteAnnotationConstructorLiteral(
                new InterceptorAnnotationParameter(arrayInitializer.toString(), arrayClass));
    }
}