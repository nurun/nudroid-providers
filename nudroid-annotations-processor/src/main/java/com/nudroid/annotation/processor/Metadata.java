package com.nudroid.annotation.processor;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;

import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.provider.delegate.ContentProviderDelegate;

/**
 * Gather all the information required to generate the source code for the content providers and router classes.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Metadata {

    private Map<String, DelegateClass> mRegisteredAuthorities = new HashMap<String, DelegateClass>();
    private Map<TypeElement, DelegateClass> mRegisteredDelegateClasses = new HashMap<TypeElement, DelegateClass>();

    /**
     * Checks if the authority has already been registered.
     * 
     * @param authorityName
     *            The authority to check for.
     * 
     * @return The {@link DelegateClass} responsible for handling the given authority. <tt>null</tt> if the authority
     *         name has not yet been registered.
     */
    public DelegateClass getDelegateClassForAuthority(String authorityName) {

        return mRegisteredAuthorities.get(authorityName);
    }

    /**
     * Gets the {@link DelegateClass} representation of the provided type element.
     * 
     * @param typeElement
     *            The {@link TypeElement} to check.
     * 
     * @return The {@link DelegateClass} for the TypeElement, or <tt>null</tt> if the {@link TypeElement} was not
     *         annotated with {@link ContentProviderDelegate}.
     */
    public DelegateClass getDelegateClassForTypeElement(TypeElement typeElement) {

        return mRegisteredDelegateClasses.get(typeElement);
    }

    /**
     * Registers an authority and the corresponding annotated {@link TypeElement}.
     * 
     * @param authorityName
     *            The authority name.
     * @param delegateClassType
     *            The delegate class responsible for handling the authority.
     */
    public void registerAuthorityHandler(String authorityName, TypeElement delegateClassType) {

        final DelegateClass delegateClass = new DelegateClass(authorityName, delegateClassType);
        mRegisteredAuthorities.put(authorityName, delegateClass);
        mRegisteredDelegateClasses.put(delegateClassType, delegateClass);
    }
}
