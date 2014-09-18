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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.InterceptorAnnotationBlueprint;

/**
 * Gather all the information required to generate the source code for the content providers and router classes.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Metadata {

    private Map<String, DelegateClass> mRegisteredAuthorities = new HashMap<>();
    private Map<TypeElement, DelegateClass> mRegisteredDelegateClasses = new HashMap<>();
    private Map<TypeElement, InterceptorAnnotationBlueprint> mInterceptorBlueprints = new HashMap<>();

    /*
     * The stack is what tracks classes to be fed to the source code generator. Because of Eclipse's continuous build,
     * sometimes multiple rounds will be triggered for the same class and source code generation will be triggered. The
     * Filer utility class will then fail because it can't create multiple instances of the same source file. The stack
     * is filled only once so each class is written only once.
     */
    private Set<DelegateClass> mDelegateClassStack = new HashSet<>();
    private Set<InterceptorAnnotationBlueprint> mInterceptorBlueprintStack = new HashSet<>();

    /**
     * Registers an authority and the corresponding annotated {@link TypeElement}.
     *
     * @param authorityName
     *         The authority name.
     * @param delegateClassType
     *         The delegate class responsible for handling the authority.
     */
    void registerNewDelegateClass(String authorityName, TypeElement delegateClassType) {

        final DelegateClass delegateClass = new DelegateClass(authorityName, delegateClassType);
        mRegisteredAuthorities.put(authorityName, delegateClass);
        mRegisteredDelegateClasses.put(delegateClassType, delegateClass);
        mDelegateClassStack.add(delegateClass);
    }

    /**
     * Pops a delegate class from the metadata (should be called after source code is generated).
     *
     * @param delegateClass
     *         The delegate class to pop out.
     */
    void popDelegateClass(DelegateClass delegateClass) {

        mDelegateClassStack.remove(delegateClass);
    }

    /**
     * Stores a new concrete annotation metadata.
     *
     * @param annotation
     *         The concrete annotation bean to register.
     */
    void registerConcreteAnnotation(InterceptorAnnotationBlueprint annotation) {

        this.mInterceptorBlueprints.put(annotation.getTypeElement(), annotation);
        this.mInterceptorBlueprintStack.add(annotation);
    }

    /**
     * Pops a concrete annotation class from the metadata (should be called after source code is generated).
     *
     * @param concreteAnnotation
     *         The concrete annotation class to pop out.
     */
    void popInterceptorBlueprint(InterceptorAnnotationBlueprint concreteAnnotation) {

        mInterceptorBlueprintStack.remove(concreteAnnotation);
    }

    /**
     * Gets the set of delegate classes to generate source code for.
     *
     * @return The set of delegate classes to generate source code for.
     */
    Set<DelegateClass> getDelegateClassesForRound() {

        return mDelegateClassStack;
    }

    /**
     * Checks if the authority has already been registered.
     *
     * @param authorityName
     *         The authority to check for.
     *
     * @return The {@link DelegateClass} responsible for handling the given authority. <tt>null</tt> if the authority
     * name has not yet been registered.
     */
    DelegateClass getDelegateClassForAuthority(String authorityName) {

        return mRegisteredAuthorities.get(authorityName);
    }

    /**
     * Gets the {@link DelegateClass} representation of the provided type element.
     *
     * @param typeElement
     *         The {@link TypeElement} to check.
     *
     * @return The {@link DelegateClass} for the {@link TypeElement}, or <tt>null</tt> the type element is not a
     * delegate class.
     */
    DelegateClass getDelegateClassForTypeElement(TypeElement typeElement) {

        return mRegisteredDelegateClasses.get(typeElement);
    }

    /**
     * Gets the set of registered concrete annotations to generate source code for.
     *
     * @return The set of registered concrete annotations to generate source code for.
     */
    Set<InterceptorAnnotationBlueprint> getInterceptorBlueprintsForRound() {

        return mInterceptorBlueprintStack;
    }

    /**
     * Gets the set of registered concrete annotations to generate source code for.
     *
     * @return The set of registered concrete annotations to generate source code for.
     */
    Collection<InterceptorAnnotationBlueprint> getInterceptorBlueprints() {

        return mInterceptorBlueprints.values();
    }

    /**
     * Gets the interceptor blueprint for the given annotation.
     *
     * @param interceptorAnnotationTypeElement
     *         The annotation to get the blueprint for.
     *
     * @return the interceptor blueprint for the given annotation.
     */
    InterceptorAnnotationBlueprint getInterceptorBlueprint(TypeElement interceptorAnnotationTypeElement) {

        return mInterceptorBlueprints.get(interceptorAnnotationTypeElement);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Metadata [mRegisteredAuthorities=" + mRegisteredAuthorities + ", \nmRegisteredDelegateClasses=" +
                mRegisteredDelegateClasses + ", \nmConcreteAnnotations=" + mInterceptorBlueprints +
                ", \nmConcreteAnnotationValues=" + mInterceptorBlueprintStack + ", \nmDelegateClassValues=" +
                mDelegateClassStack + "]";
    }
}
