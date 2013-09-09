/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.nurun.persistence.annotation.Authority;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class Metadata {

    private Map<TypeElement, Set<ExecutableElement>> classesAndMethods = new HashMap<TypeElement, Set<ExecutableElement>>();
    private Map<String, Set<ExecutableElement>> urisAndMethods = new HashMap<String, Set<ExecutableElement>>();
    private Map<String, Set<String>> authoritiesAndUris = new HashMap<String, Set<String>>();
    private Map<String, TypeElement> authorityToClass = new HashMap<String, TypeElement>();
    private Map<TypeElement, String> classToAuthority = new HashMap<TypeElement, String>();
    private HashSet<String> indexedTypeElements = new HashSet<String>();

    /**
     * @param enclosingElement
     * @param elem
     */
    public void addTargetMethod(TypeElement enclosingElement, ExecutableElement elem) {

        Set<ExecutableElement> methodSet = classesAndMethods.get(enclosingElement);

        if (methodSet == null) {

            methodSet = new HashSet<ExecutableElement>();
            methodSet.add(elem);
            classesAndMethods.put(enclosingElement, methodSet);
        } else {

            methodSet.add(elem);
        }
    }

    /**
     * @return
     */
    public Set<TypeElement> getTargetClasses() {

        return classesAndMethods.keySet();
    }

    /**
     * @param targetElement
     * @return
     */
    public Set<ExecutableElement> getMethodsForClass(TypeElement targetElement) {
        return classesAndMethods.get(targetElement);
    }

    /**
     * @param value
     * @param element
     */
    public void addUri(String value, ExecutableElement element) {

        Set<ExecutableElement> methodSet = urisAndMethods.get(element);

        if (methodSet == null) {

            methodSet = new HashSet<ExecutableElement>();
            methodSet.add(element);
            urisAndMethods.put(value, methodSet);
        } else {

            methodSet.add(element);
        }
    }

    /**
     * @return
     */
    public Map<String, Set<String>> getAuthoritiesAndUris() {

        return Collections.unmodifiableMap(authoritiesAndUris);
    }

    /**
     * @param authority
     * @param uri
     */
    public void addUri(String authority, String uri) {

        Set<String> uris = authoritiesAndUris.get(authority);

        if (uris == null) {

            uris = new HashSet<String>();
            uris.add(new Uri(uri).getPath());
            authoritiesAndUris.put(authority, uris);
        } else {

            uris.add(new Uri(uri).getPath());
        }
    }

    public TypeElement getClassForAuthority(String string) {

        return authorityToClass.get(string);
    }

    /**
     * @param value
     * @param enclosingElement
     */
    public void setClassForAuthority(String value, TypeElement typeElement) {

        authorityToClass.put(value, typeElement);
    }
    
    public void indexedType(Element element) {
        
        indexedTypeElements.add(element.toString());
    }

    /**
     * @return
     */
    public Set<String> getIndexedTypeNames() {

        return Collections.unmodifiableSet(indexedTypeElements);
    }

    public String parseAuthorityFromClass(Element classRoot) {

        Authority authority = classRoot.getAnnotation(Authority.class);
        String authorityName = authority != null ? authority.value() : classRoot.toString();

        authorityName = authorityName.equals("") ? classRoot.toString() : authorityName;

        return authorityName;
    }
}
