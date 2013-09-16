package com.nudroid.persistence.annotation.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;

/**
 * Holds information about the delegate class for a content provider.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateClass {

    private static final String ROUTER_SUFFIX = "Router";
    private String className;
    private String simpleName;
    private Map<Integer, Set<DelegateMethod>> delegateMethods = new HashMap<Integer, Set<DelegateMethod>>();
    @SuppressWarnings("unused")
    private LoggingUtils logger;

    /**
     * Creates an instance of this class.
     * 
     * @param element
     *            The {@link Element} for the delegate class as provided by a round environment.
     * @param logger
     *            An instance of the logging utils.
     */
    DelegateClass(Element element, LoggingUtils logger) {

        this.logger = logger;
        this.className = element.toString().toString();
        this.simpleName = element.getSimpleName().toString();
    }

    /**
     * Adds a representation of a delegate method to this class.
     * 
     * @param delegateMethod
     *            The method to be added.
     */
    void addMethod(DelegateMethod delegateMethod) {

        Set<DelegateMethod> setForUriId = delegateMethods.get(delegateMethod.getUriId());

        if (setForUriId == null) {

            setForUriId = new HashSet<DelegateMethod>();
            setForUriId.add(delegateMethod);
            delegateMethods.put(delegateMethod.getUriId(), setForUriId);
        } else {

            setForUriId.add(delegateMethod);
        }
    }

    /**
     * Gets the fully qualified name of the delegate class.
     * 
     * @return The fully qualified name of the delegate class.
     */
    public String getName() {
        return className;
    }

    /**
     * Gets the simple name of the delegate class (i.e. without the package name).
     * 
     * @return The qualified name of the delegate class (i.e. without the package name).
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * Gets the mapping of uri ids to delegate methods responsible to process requests matching that uri.
     * 
     * @return The mapping of uri ids to delegate methods responsible to process requests matching that uri.
     */
    public Map<Integer, Set<DelegateMethod>> getUriToDelegateMethodMap() {

        return Collections.unmodifiableMap(delegateMethods);
    }

    /**
     * Gets all the URI ids this class is responsible to process.
     * 
     * @return All the URI ids this class is responsible to process.
     */
    public Set<Integer> getUriIds() {
        return delegateMethods.keySet();
    }

    /**
     * Gets the simple name of the router class which will be created to route calls to the delegate class (i.e. without
     * the package name).
     * 
     * @return The simple name of the router class which will be created to route calls to the delegate class (i.e.
     *         without the package name).
     */
    public String getRouterSimpleName() {

        return simpleName + ROUTER_SUFFIX;
    }
}