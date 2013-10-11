package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import com.nudroid.annotation.processor.DelegateMethod;
import com.nudroid.annotation.processor.DuplicatePathException;

/**
 * Holds information about the delegate class for a content provider.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateClass {

    private static final String ROUTER_SUFFIX = "Router";
    private String mQualifiedName;
    private String mSimpleName;
    private Map<Integer, Set<DelegateMethod>> delegateMethods = new HashMap<Integer, Set<DelegateMethod>>();
    private List<MatcherUri> mMactherUris = new ArrayList<MatcherUri>();
    private List<DelegateUri> mDelegateUris = new ArrayList<DelegateUri>();
    private Authority authority;
    private int mMatcherUriIdCount;

    /**
     * Creates an instance of this class.
     * 
     * @param authorityName
     *            The authority name being handled by the delegate class.
     * 
     * @param element
     *            The {@link Element} for the delegate class as provided by a round environment.
     */
    public DelegateClass(String authorityName, Element element) {

        this.mQualifiedName = element.toString();
        this.mSimpleName = element.getSimpleName().toString();
        this.authority = new Authority(authorityName, this);
    }

    /**
     * Adds a representation of a delegate method to this class.
     * 
     * @param delegateMethod
     *            The method to be added.
     */
    public void addMethod(DelegateMethod delegateMethod) {

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
        return mQualifiedName;
    }

    /**
     * Gets the simple name of the delegate class (i.e. without the package name).
     * 
     * @return The qualified name of the delegate class (i.e. without the package name).
     */
    public String getmSimpleName() {
        return mSimpleName;
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

        return mSimpleName + ROUTER_SUFFIX;
    }

    /**
     * Gets the authority associated with this class.
     * 
     * @return The authority associated with this class.
     */
    public Authority getAuthority() {
        return authority;
    }

    /**
     * Registers a path to be matched by the content provider uri matcher.
     * 
     * @param queryMethodElement
     *            The method the path appears on.
     * 
     * @param path
     *            The path to register.
     */
    public DelegateUri registerPath(ExecutableElement queryMethodElement, String path) {

        MatcherUri matcherUri = new MatcherUri(authority, path);

        if (!mMactherUris.contains(matcherUri)) {
            matcherUri.setId(++mMatcherUriIdCount);
            mMactherUris.add(matcherUri);
        }

        DelegateUri delegateUri = new DelegateUri(mMactherUris.get(mMactherUris.indexOf(matcherUri)), path);

        if (mDelegateUris.contains(delegateUri)) {

            throw new DuplicatePathException("");
        }

        mDelegateUris.add(delegateUri);
        return delegateUri;
    }
}