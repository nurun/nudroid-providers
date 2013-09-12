package com.nudroid.persistence.annotation.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateClass {

    private static final String ROUTER_SUFFIX = "Router";
    private String className;
    private String simpleName;
    private Map<Integer, Set<DelegateMethod>> delegateMethods = new HashMap<Integer, Set<DelegateMethod>>();
    private LoggingUtils logger;

    DelegateClass(Element element, LoggingUtils logger) {

        this.logger = logger;
        this.className = element.toString().toString();
        this.simpleName = element.getSimpleName().toString();
    }

    void addMethod(DelegateMethod delegateMethod) {

        Set<DelegateMethod> setForUriId = delegateMethods.get(delegateMethod.getUriId());

        if (setForUriId == null) {

            setForUriId = new HashSet<DelegateMethod>();
            setForUriId.add(delegateMethod);
            delegateMethods.put(delegateMethod.getUriId(), setForUriId);
        } else {

            setForUriId.add(delegateMethod);
        }
        
        logger.warn("Added method to class. Map is now " + delegateMethods);
    }

    public String getName() {
        return className;
    }

    public String getSimpleName() {
        return simpleName;
    }
    
    public Map<Integer, Set<DelegateMethod>> getUriDelegateMethodMap() {

        logger.warn("Returning class methods " + Collections.unmodifiableMap(delegateMethods));
        return Collections.unmodifiableMap(delegateMethods);
    }
    
    public Set<Integer> getUriIds() {
        return delegateMethods.keySet();
    }

    public String getRouterSimpleName() {

        return simpleName + ROUTER_SUFFIX;
    }
}
