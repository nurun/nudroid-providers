/**
 * 
 */
package com.nudroid.annotation.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class Metadata {

    private Map<TypeElement, Set<ExecutableElement>> classesAndMethods = new HashMap<TypeElement, Set<ExecutableElement>>();
    private Map<String, Set<ExecutableElement>> urisAndMethods = new HashMap<String, Set<ExecutableElement>>();
    private Map<String, Set<String>> authoritiesAndUris = new HashMap<String, Set<String>>();
    private Map<String, TypeElement> authoritiesAndClasses = new HashMap<String, TypeElement>();
    private HashSet<String> indexedTypeElements;

    public void init() {

        
        
        if (Thread.currentThread().getContextClassLoader().getResource("com/nudroid/annotation/processor/index.index") == null) {

            indexedTypeElements = new HashSet<String>();
        } else {

            loadTypeElementsFromIndexFile();
        }
    }

    private void loadTypeElementsFromIndexFile() {

        Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("com/nudroid/annotation/processor/index.index"));

        indexedTypeElements = new HashSet<String>();

        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            indexedTypeElements.add(nextLine);
        }

        scanner.close();
    }

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

    /**
     * @param string
     * @return
     */
    public TypeElement getClassForAuthority(String string) {

        return authoritiesAndClasses.get(string);
    }

    /**
     * @param value
     * @param enclosingElement
     */
    public void addClassAuthority(String value, TypeElement typeElement) {

        authoritiesAndClasses.put(value, typeElement);
    }

    /**
     * @return
     */
    public Set<String> getIndexedTypes() {

        return Collections.unmodifiableSet(indexedTypeElements);
    }
}
