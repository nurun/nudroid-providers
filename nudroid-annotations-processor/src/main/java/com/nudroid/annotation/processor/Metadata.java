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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.DelegateMethod;
import com.nudroid.annotation.processor.model.InterceptorBlueprint;

/**
 * Gather all the information required to generate the source code for the content providers and router classes.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Metadata {

    private Map<String, DelegateClass> mRegisteredAuthorities = new HashMap<String, DelegateClass>();
    private Map<TypeElement, DelegateClass> mRegisteredDelegateClasses = new HashMap<TypeElement, DelegateClass>();
    private Map<ExecutableElement, DelegateMethod> mRegisteredDelegateMethods = new HashMap<ExecutableElement, DelegateMethod>();
    private Map<TypeElement, InterceptorBlueprint> mInterceptorBlueprints = new HashMap<TypeElement, InterceptorBlueprint>();
    private Set<InterceptorBlueprint> mInterceptorBlueprintStack = new HashSet<InterceptorBlueprint>();
    private Set<DelegateClass> mDelegateClassStack = new HashSet<DelegateClass>();

    /**
     * Registers an authority and the corresponding annotated {@link TypeElement}.
     * 
     * @param authorityName
     *            The authority name.
     * @param delegateClassType
     *            The delegate class responsible for handling the authority.
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
     *            The delegate class to pop out.
     */
    void popDelegateClass(DelegateClass delegateClass) {

        mDelegateClassStack.remove(delegateClass);
    }

    /**
     * Registers a delegate method.
     * 
     * @param executableElement
     *            The {@link ExecutableElement} for the method.
     * @param delagateMethod
     *            The delegate method.
     */
    void registerDelegateMethod(ExecutableElement executableElement, DelegateMethod delagateMethod) {

        mRegisteredDelegateMethods.put(executableElement, delagateMethod);
    }

    /**
     * Stores a new concrete annotation metadata.
     * 
     * @param annotation
     *            The concrete annotation bean to register.
     */
    void registerConcreteAnnotation(InterceptorBlueprint annotation) {

        this.mInterceptorBlueprints.put(annotation.getTypeElement(), annotation);
        this.mInterceptorBlueprintStack.add(annotation);
    }

    /**
     * Pops a concrete annotation class from the metadata (should be called after source code is generated).
     * 
     * @param concreteAnnotation
     *            The concrete annotation class to pop out.
     */
    void popInterceptorBlueprint(InterceptorBlueprint concreteAnnotation) {

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
     *            The authority to check for.
     * 
     * @return The {@link DelegateClass} responsible for handling the given authority. <tt>null</tt> if the authority
     *         name has not yet been registered.
     */
    DelegateClass getDelegateClassForAuthority(String authorityName) {

        return mRegisteredAuthorities.get(authorityName);
    }

    /**
     * Gets the {@link DelegateClass} representation of the provided type element.
     * 
     * @param typeElement
     *            The {@link TypeElement} to check.
     * 
     * @return The {@link DelegateClass} for the {@link TypeElement}, or <tt>null</tt> the type element is not a
     *         delegate class.
     */
    DelegateClass getDelegateClassForTypeElement(TypeElement typeElement) {

        return mRegisteredDelegateClasses.get(typeElement);
    }

    /**
     * Gets the {@link DelegateMethod} representation of the provided element.
     * 
     * @param executableElement
     *            The {@link ExecutableElement} to check.
     * @return The {@link DelegateMethod} for the {@link ExecutableElement}, or <tt>null</tt> if the executable element
     *         is not a delegate method.
     */
    DelegateMethod getDelegateMethodForElement(ExecutableElement executableElement) {

        return mRegisteredDelegateMethods.get(executableElement);
    }

    /**
     * Gets the set of registered concrete annotations to generate source code for.
     * 
     * @return The set of registered concrete annotations to generate source code for.
     */
    Set<InterceptorBlueprint> getInterceptorBlueprintsForRound() {

        return mInterceptorBlueprintStack;
    }

    /**
     * Gets the set of registered concrete annotations to generate source code for.
     * 
     * @return The set of registered concrete annotations to generate source code for.
     */
    Collection<InterceptorBlueprint> getInterceptorBlueprints() {

        return mInterceptorBlueprints.values();
    }

    /**
     * Gets the interceptor blueprint for the given annotation.
     * 
     * @param interceptorAnnotationTypeElement
     *            The annotation to get the blueprint for.
     * 
     * @return the interceptor blueprint for the given annotation.
     */
    InterceptorBlueprint getInterceptorBlueprint(TypeElement interceptorAnnotationTypeElement) {

        return mInterceptorBlueprints.get(interceptorAnnotationTypeElement);
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Metadata [mRegisteredAuthorities=" + mRegisteredAuthorities + ", \nmRegisteredDelegateClasses="
                + mRegisteredDelegateClasses + ", \nmRegisteredDelegateMethods=" + mRegisteredDelegateMethods
                + ", \nmConcreteAnnotations=" + mInterceptorBlueprints + ", \nmConcreteAnnotationValues="
                + mInterceptorBlueprintStack + ", \nmDelegateClassValues=" + mDelegateClassStack + "]";
    }
}
