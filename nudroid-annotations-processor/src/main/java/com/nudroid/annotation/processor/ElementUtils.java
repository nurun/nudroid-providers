package com.nudroid.annotation.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/**
 * Utility classes for getting information about {@link Element}s.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class ElementUtils {

    /**
     * Checks if the element is abstract.
     * 
     * @param element
     *            Tfe element to check.
     * @return <tt>true</tt> if the element is abstract, <tt>false</tt> otherwise.
     */
    static boolean isAbstract(Element element) {

        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    /**
     * Checks if the element is a class or interface.
     * 
     * @param element
     *            The element to check.
     * @return <tt>true</tt> if the element is a class or interface, <tt>false</tt> otherwise.
     */
    static boolean isClassOrInterface(Element element) {

        return element.getKind().equals(ElementKind.CLASS) || element.getKind().equals(ElementKind.INTERFACE);
    }

    /**
     * Checks if the element is an interface.
     * 
     * @param element
     *            The element to check.
     * @return <tt>true</tt> if the element is an interface, <tt>false</tt> otherwise.
     */
    static boolean isInterface(Element element) {

        return element.getKind().equals(ElementKind.INTERFACE);
    }

    /**
     * Checks if the element is a class.
     * 
     * @param element
     *            The element to check.
     * @return <tt>true</tt> if the element is a class, <tt>false</tt> otherwise.
     */
    static boolean isClass(Element element) {

        return element.getKind().equals(ElementKind.CLASS);
    }
}
