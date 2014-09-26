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

package com.nudroid.annotation.processor;

import android.content.Context;
import android.net.Uri;

import com.nudroid.provider.interceptor.ContentProviderInterceptor;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Utility classes for getting information about source code elements.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class ProcessorUtils {

    private final Types typeUtils;
    private final Elements elementUtils;
    private final TypeMirror ANDROID_CONTEXT_TYPE_MIRROR;
    private final TypeMirror STRING_TYPE_MIRROR;
    private final TypeMirror CONTENT_PROVIDER_INTERCEPTOR_TYPE_MIRROR;
    private final ArrayType STRING_ARRAY_TYPE_MIRROR;
    private final TypeMirror ANDROID_URI_TYPE_MIRROR;

    public ProcessorUtils(Types typeUtils, Elements elementUtils) {

        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;

        TypeElement androidContextType = elementUtils.getTypeElement(Context.class.getName());
        TypeElement stringType = elementUtils.getTypeElement(String.class.getName());
        TypeElement contentProviderInterceptorType =
                elementUtils.getTypeElement(ContentProviderInterceptor.class.getName());
        TypeElement androidUriType = elementUtils.getTypeElement(Uri.class.getName());

        this.ANDROID_CONTEXT_TYPE_MIRROR = androidContextType.asType();
        this.STRING_TYPE_MIRROR = stringType.asType();
        this.STRING_ARRAY_TYPE_MIRROR = typeUtils.getArrayType(STRING_TYPE_MIRROR);
        this.CONTENT_PROVIDER_INTERCEPTOR_TYPE_MIRROR = contentProviderInterceptorType.asType();
        this.ANDROID_URI_TYPE_MIRROR = androidUriType.asType();
    }

    /**
     * Checks if the type element implements the given interface.
     *
     * @param element
     *         the element to check
     * @param interfaceClass
     *         the Class of the interface to check
     *
     * @return <tt>true</tt> if it does and the provided class is an interface, <tt>false</tt> otherwise
     */
    public boolean implementsInterface(TypeElement element, Class<?> interfaceClass) {

        TypeElement contentProviderInterceptorType = elementUtils.getTypeElement(interfaceClass.getName());

        return contentProviderInterceptorType.getKind() == ElementKind.INTERFACE &&
                typeUtils.isAssignable(element.asType(), elementUtils.getTypeElement(interfaceClass.getName())
                        .asType());
    }

    /**
     * Given a type element, construct a valid java identifier composed of every enclosing class.
     * <p>
     * For example, given:
     * <pre>
     *     class A {
     *         static class B {
     *             static class C {
     *
     *             }
     *         }
     *     }
     * </pre>
     * And requesting a composite name for TypeElement C yields "A$B$C".
     *
     * @param typeElement
     *         the type element to generate a name for
     *
     * @return the name for the type element
     */
    public String generateCompositeElementName(TypeElement typeElement) {

        Element parentElement = typeElement.getEnclosingElement();
        StringBuilder concreteSimpleName = new StringBuilder(typeElement.getSimpleName());

        while (parentElement != null && !parentElement.getKind()
                .equals(ElementKind.PACKAGE)) {

            concreteSimpleName.insert(0, "$")
                    .insert(0, parentElement.getSimpleName());
            parentElement = parentElement.getEnclosingElement();
        }

        return concreteSimpleName.toString();
    }

    /**
     * Given an annotation mirror, returns the values of the annotations properties in source code, including default
     * values.
     *
     * @param annotationMirror
     *         the annotation to inspect
     *
     * @return a map containing the values of the annotation attributes
     */
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getAnnotationValuesWithDefaults(
            AnnotationMirror annotationMirror) {

        return elementUtils.getElementValuesWithDefaults(annotationMirror);
    }

    /**
     * Finds the package a type element is declared. Will traverse any enclosing elements until the package element is
     * reached.
     *
     * @param typeElement
     *         the type element
     *
     * @return the package of the type element
     */
    public PackageElement findPackage(TypeElement typeElement) {

        Element element = typeElement;

        do {
            element = element.getEnclosingElement();
        } while (element != null && element.getKind() != ElementKind.PACKAGE);

        return (PackageElement) element;
    }

    /**
     * Checks if the provided type mirror is an Android Context class (android.content.Context).
     *
     * @param type
     *         the type to check
     *
     * @return @return <tt>true</tt> if it is, <tt>false</tt> otherwise
     */
    public boolean isAndroidContext(TypeMirror type) {

        return typeUtils.isSameType(type, ANDROID_CONTEXT_TYPE_MIRROR);
    }

    /**
     * Checks if the provided type mirror is an array of Strings.
     *
     * @param type
     *         the type to check
     *
     * @return @return <tt>true</tt> if it is, <tt>false</tt> otherwise
     */
    public boolean isArrayOfStrings(TypeMirror type) {

        return typeUtils.isSameType(type, STRING_ARRAY_TYPE_MIRROR);
    }

    /**
     * Checks if the provided type mirror is an String.
     *
     * @param type
     *         the type to check
     *
     * @return @return <tt>true</tt> if it is, <tt>false</tt> otherwise
     */
    public boolean isString(TypeMirror type) {

        return typeUtils.isSameType(type, STRING_TYPE_MIRROR);
    }

    /**
     * Checks if the provided type mirror is a Class.
     *
     * @param type
     *         the type to check
     *
     * @return @return <tt>true</tt> if it is, <tt>false</tt> otherwise
     */
    public boolean isClass(TypeMirror type) {

        return typeUtils.asElement(type)
                .getKind() == ElementKind.CLASS;
    }

    /**
     * Checks if the provided type mirror is an enum.
     *
     * @param type
     *         the type to check
     *
     * @return @return <tt>true</tt> if it is, <tt>false</tt> otherwise
     */
    public boolean isEnum(TypeMirror type) {

        return typeUtils.asElement(type)
                .getKind() == ElementKind.ENUM;
    }

    /**
     * Checks if the provided type mirror is an Android Uri (android.net.Uri).
     *
     * @param type
     *         the type to check
     *
     * @return @return <tt>true</tt> if it is, <tt>false</tt> otherwise
     */
    public boolean isAndroidUri(TypeMirror type) {

        return typeUtils.isSameType(type, ANDROID_URI_TYPE_MIRROR);
    }

    /**
     * Checks if two types are the same.
     *
     * @param type1
     *         the first type
     * @param type2
     *         the second type
     *
     * @return @return <tt>true</tt> if they are, <tt>false</tt> otherwise
     */
    public boolean isSame(TypeMirror type1, TypeMirror type2) {

        return typeUtils.isSameType(type1, type2);
    }

    /**
     * Checks if the element is abstract.
     *
     * @param element
     *         Tfe element to check.
     *
     * @return <tt>true</tt> if the element is abstract, <tt>false</tt> otherwise.
     */
    public boolean isAbstract(Element element) {

        return element.getModifiers()
                .contains(Modifier.ABSTRACT);
    }

    /**
     * Checks if the element is a class or interface (but not an enum or annotation).
     *
     * @param element
     *         The element to check.
     *
     * @return <tt>true</tt> if the element is a class or interface, <tt>false</tt> otherwise.
     */
    public boolean isClassOrInterface(Element element) {

        return element.getKind()
                .equals(ElementKind.CLASS) || element.getKind()
                .equals(ElementKind.INTERFACE);
    }

    /**
     * Checks if the element is a class (but not an enum).
     *
     * @param element
     *         The element to check.
     *
     * @return <tt>true</tt> if the element is a class, <tt>false</tt> otherwise.
     */
    public boolean isClass(Element element) {

        return element.getKind()
                .equals(ElementKind.CLASS);
    }
}
