/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class DelegateClass {

    private String className;
    private String simpleName;
    private List<DelegateMethod> methods = new LinkedList<DelegateMethod>();
    private Set<Integer> uriIds = new HashSet<Integer>();

    public DelegateClass(String className) {
        this.className = className;
    }

    public void addMethod(DelegateMethod method) {
        
        methods.add(method);
        uriIds.add(method.getUriId());
        System.out.println("&&&&&&&&&&&&&&&&Added uriid to class " + uriIds);
    }

    @Override
    public String toString() {
        return "DelegateClass [className=" + className + ", methods=" + methods + "]";
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getName() {
        return className;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public List<DelegateMethod> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    /**
     * @return
     */
    public String getRouterName() {

        return simpleName + "Router";
    }

    public Set<Integer> getUriIds() {
        return Collections.unmodifiableSet(uriIds);
    }
}
