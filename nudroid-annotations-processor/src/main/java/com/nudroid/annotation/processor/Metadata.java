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

import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.InterceptorPointAnnotationBlueprint;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;

/**
 * Gather all the information required to generate the source code for the content providers and router classes.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Metadata {

    private final Map<String, DelegateClass> registeredAuthorities = new HashMap<>();
    private final Map<TypeElement, DelegateClass> registeredDelegateClasses = new HashMap<>();
    private final Map<TypeElement, InterceptorPointAnnotationBlueprint> interceptorPointAnnotationBlueprints =
            new HashMap<>();

    /*
     * Source code generation is triggered by multiple rounds. Each round can also add more classes to be processed.
     * Keep the pile of classes to be processed by a particular round separate from the metadata.
     *
     * TODO: Currently, the source code generator has to remove a processed class from the pile. Instead,
     * consider grouping classes by round number and the source code generator process classes for that round only.
     */
    private final Set<DelegateClass> mDelegateClassPile = new HashSet<>();
    private final Set<InterceptorPointAnnotationBlueprint> mInterceptorPointAnnotationBlueprintPile = new HashSet<>();

    /**
     * Registers an authority and the corresponding annotated {@link TypeElement}.
     *
     * @param authorityName
     *         the authority name
     * @param delegateClassType
     *         the delegate class responsible for handling the authority
     *
     * @return the new delegate class
     */
    DelegateClass registerNewDelegateClass(String authorityName, TypeElement delegateClassType) {

        final DelegateClass delegateClass = new DelegateClass(authorityName, delegateClassType);
        registeredAuthorities.put(authorityName, delegateClass);
        registeredDelegateClasses.put(delegateClassType, delegateClass);
        mDelegateClassPile.add(delegateClass);
        return delegateClass;
    }

    /**
     * Pops a delegate class from the metadata (should be called after source code is generated).
     *
     * @param delegateClass
     *         The delegate class to pop out.
     */
    void popDelegateClass(DelegateClass delegateClass) {

        mDelegateClassPile.remove(delegateClass);
    }

    /**
     * Stores a new concrete annotation metadata.
     *
     * @param annotation
     *         The concrete annotation bean to register.
     */
    void registerAnnotationBlueprint(InterceptorPointAnnotationBlueprint annotation) {

        this.interceptorPointAnnotationBlueprints.put(annotation.getTypeElement(), annotation);
        this.mInterceptorPointAnnotationBlueprintPile.add(annotation);
    }

    /**
     * Pops a concrete annotation class from the metadata (should be called after source code is generated).
     *
     * @param concreteAnnotation
     *         The concrete annotation class to pop out.
     */
    void popInterceptorBlueprint(InterceptorPointAnnotationBlueprint concreteAnnotation) {

        mInterceptorPointAnnotationBlueprintPile.remove(concreteAnnotation);
    }

    /**
     * Gets the set of delegate classes to generate source code for.
     *
     * @return The set of delegate classes to generate source code for.
     */
    Set<DelegateClass> getDelegateClassesForRound() {

        return mDelegateClassPile;
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

        return registeredAuthorities.get(authorityName);
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

        return registeredDelegateClasses.get(typeElement);
    }

    /**
     * Gets the set of registered concrete annotations to generate source code for.
     *
     * @return The set of registered concrete annotations to generate source code for.
     */
    Set<InterceptorPointAnnotationBlueprint> getInterceptorBlueprintsForRound() {

        return mInterceptorPointAnnotationBlueprintPile;
    }

    /**
     * Gets the set of registered concrete annotations to generate source code for.
     *
     * @return The set of registered concrete annotations to generate source code for.
     */
    Collection<InterceptorPointAnnotationBlueprint> getInterceptorBlueprints() {

        return interceptorPointAnnotationBlueprints.values();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Metadata [registeredAuthorities=" + registeredAuthorities + ", \nregisteredDelegateClasses=" +
                registeredDelegateClasses + ", \nmConcreteAnnotations=" + interceptorPointAnnotationBlueprints +
                ", \nmConcreteAnnotationValues=" + mInterceptorPointAnnotationBlueprintPile +
                ", \nmDelegateClassValues=" +
                mDelegateClassPile + "]";
    }
}
