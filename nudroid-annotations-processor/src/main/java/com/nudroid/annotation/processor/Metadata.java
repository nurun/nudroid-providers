package com.nudroid.annotation.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.nudroid.annotation.processor.model.ConcreteAnnotation;
import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.DelegateMethod;

/**
 * Gather all the information required to generate the source code for the content providers and router classes.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Metadata {

    private Map<String, DelegateClass> mRegisteredAuthorities = new HashMap<String, DelegateClass>();
    private Map<TypeElement, DelegateClass> mRegisteredDelegateClasses = new HashMap<TypeElement, DelegateClass>();
    private Map<ExecutableElement, DelegateMethod> mRegisteredDelegateMethods = new HashMap<ExecutableElement, DelegateMethod>();
    private Map<TypeElement, ConcreteAnnotation> mConcreteAnnotations = new HashMap<TypeElement, ConcreteAnnotation>();
    private Set<ConcreteAnnotation> mConcreteAnnotationValues;
    private Set<DelegateClass> mDelegateClassValues;

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
    void registerConcreteAnnotation(ConcreteAnnotation annotation) {

        this.mConcreteAnnotations.put(annotation.getTypeElement(), annotation);
    }

    /**
     * Gets the set of delegate classes to generate source code to.
     * 
     * @return The set of delegate classes to generate source code to.
     */
    Set<DelegateClass> getDelegateClasses() {

        if (mDelegateClassValues == null) {

            mDelegateClassValues = new HashSet<DelegateClass>();
            mDelegateClassValues.addAll(mRegisteredDelegateClasses.values());
        }

        return mDelegateClassValues;
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
     * Gets the set of registered concrete annotations.
     * 
     * @return The set of registered concrete annotations.
     */
    Set<ConcreteAnnotation> getConcreteAnnotations() {

        if (mConcreteAnnotationValues == null) {

            mConcreteAnnotationValues = new HashSet<ConcreteAnnotation>();
            mConcreteAnnotationValues.addAll(mConcreteAnnotations.values());
        }

        return mConcreteAnnotationValues;
    }

    /**
     * @param interceptorAnnotation
     * @return
     */
    ConcreteAnnotation getConcreteAnnotation(TypeElement interceptorAnnotation) {

        return mConcreteAnnotations.get(interceptorAnnotation);
    }
}
