package com.nudroid.annotation.processor.model;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.nudroid.annotation.processor.DuplicatePathException;
import com.nudroid.annotation.provider.delegate.ContentProvider;

/**
 * Holds information about the delegate class for a content provider.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateClass {

    private String mQualifiedName;
    private String mSimpleName;
    private int mMatcherUriIdCount = 0;
    private TypeElement mTypeElement;
    private boolean mHasImplementedDelegateInterface;
    private String mBasePackageName;
    private String mContentProviderSimpleName;
    private String mRouterSimpleName;
    private Authority mAuthority;

    private NavigableSet<MatcherUri> mMactherUris = new TreeSet<MatcherUri>(new Comparator<MatcherUri>() {

        /**
         * Sorts the matcher uris by uri id.
         * 
         * @see Comparator#compare(Object, Object)
         */
        @Override
        public int compare(MatcherUri o1, MatcherUri o2) {

            return o1.getId() - o2.getId();
        }
    });

    /**
     * Creates an instance of this class.
     * 
     * @param authorityName
     *            The authority name being handled by the delegate class.
     * 
     * @param typeElement
     *            The {@link TypeElement} for the delegate class as provided by the round environment.
     */
    public DelegateClass(String authorityName, TypeElement typeElement) {

        this.mTypeElement = typeElement;
        this.mQualifiedName = typeElement.toString();
        this.mSimpleName = typeElement.getSimpleName().toString();
        this.mAuthority = new Authority(authorityName);

        String baseName = this.mSimpleName;
        baseName = baseName.replaceAll("(?i)Delegate", "").replaceAll("(?i)ContentProvider", "");

        StringBuilder providerSimpleName = new StringBuilder(baseName);
        StringBuilder routerSimpleName = new StringBuilder(this.mSimpleName);
        Element parentElement = typeElement.getEnclosingElement();

        while (parentElement != null && !parentElement.getKind().equals(ElementKind.PACKAGE)) {

            providerSimpleName.insert(0, "$").insert(0, parentElement.getSimpleName());
            routerSimpleName.insert(0, "$").insert(0, parentElement.getSimpleName());
            parentElement = parentElement.getEnclosingElement();
        }

        providerSimpleName.append("ContentProvider");
        routerSimpleName.append("Router");

        if (parentElement != null && parentElement.getKind().equals(ElementKind.PACKAGE)) {

            this.mBasePackageName = ((PackageElement) parentElement).getQualifiedName().toString();
        } else {

            this.mBasePackageName = "";
        }

        this.mRouterSimpleName = routerSimpleName.toString();
        this.mContentProviderSimpleName = providerSimpleName.toString();
    }

    /**
     * Registers a @Query path and query string to be handled by this delegate class. DelegateUris, as opposed to
     * MatcherUri does take query string in consideration when differentiating between URIs.
     * 
     * @param pathAndQuery
     *            The path and query string to register.
     * 
     * @return A new DelegateUri object representing this path and query string combination.
     * 
     * @throws DuplicatePathException
     *             If the path and query string has already been associated with an existing @Query DelegateMethod.
     */
    public DelegateUri registerPathForQuery(String pathAndQuery) {

        MatcherUri matcherUri = getMatcherUriFor(pathAndQuery);
        DelegateUri delegateUri = matcherUri.registerQueryDelegateUri(pathAndQuery);

        return delegateUri;
    }

    /**
     * Registers a @Update path and query string to be handled by this delegate class. DelegateUris, as opposed to
     * MatcherUri does take query string in consideration when differentiating between URIs.
     * 
     * @param pathAndQuery
     *            The path and query string to register.
     * 
     * @return A new DelegateUri object representing this path and query string combination.
     * 
     * @throws DuplicatePathException
     *             If the path and query string has already been associated with an existing @Update DelegateMethod.
     */
    public DelegateUri registerPathForUpdate(ExecutableElement updateMethodElement, String pathAndQuery) {

        return null;
    }

    /**
     * Sets if the delegate class implements the delegate interface.
     * 
     * @param doesImplementDelegateInterface
     *            <tt>true</tt> if the delegate class implements the {@link ContentProvider} interface, <tt>false</tt>
     *            otherwise.
     */
    public void setImplementsDelegateInterface(boolean doesImplementDelegateInterface) {
        this.mHasImplementedDelegateInterface = doesImplementDelegateInterface;
    }

    /**
     * Gets the {@link TypeElement} mapped by this class.
     * 
     * @return The {@link TypeElement} mapped by this class.
     */
    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    /**
     * Gets the fully qualified name of the delegate class.
     * 
     * @return The fully qualified name of the delegate class.
     */
    public String getQualifiedName() {

        return mQualifiedName;
    }

    /**
     * Gets the simple name of the delegate class (i.e. without the package name).
     * 
     * @return The qualified name of the delegate class (i.e. without the package name).
     */
    public String getSimpleName() {
        return mSimpleName;
    }

    /**
     * Gets the authority associated with this class.
     * 
     * @return The authority associated with this class.
     */
    public Authority getAuthority() {

        return mAuthority;
    }

    /**
     * Gets the simple name of the router class which will be created to route calls to the delegate class (i.e. without
     * the package name).
     * 
     * @return The simple name of the router class which will be created to route calls to the delegate class (i.e.
     *         without the package name).
     */
    public String getRouterSimpleName() {

        return mRouterSimpleName;
    }

    /**
     * Gets the simple name of this delegate's content provider (i.e. without the package name).
     * 
     * @return The simple name of this delegate's content provider.
     */
    public String getContentProviderSimpleName() {

        return this.mContentProviderSimpleName;
    }

    /**
     * Gets the base package name for the generated class files.
     * 
     * @return The base package name for the generated source files.
     */
    public String getBasePackageName() {

        return mBasePackageName;
    }

    /**
     * Checks if the delegate class implements the {@link ContentProvider} interface.
     * 
     * @return <tt>true</tt> if the delegate class implements the {@link ContentProvider} interface, <tt>false</tt>
     *         otherwise.
     */
    public boolean hasContentProviderDelegateInterface() {

        return mHasImplementedDelegateInterface;
    }

    /**
     * Gets the {@link MatcherUri}s this delegate class accepts.
     * 
     * @return The {@link MatcherUri}s this delegate class accepts.
     */
    public NavigableSet<MatcherUri> getMactherUris() {

        return mMactherUris;
    }

    /**
     * Checks if a {@link MatcherUri} has already been created for the path and query. If yes, returns that instance. If
     * not, a new one is created and registered.
     * 
     * @param pathAndQuery
     *            The path and query to check.
     * 
     * @return The {@link MatcherUri} for the path and query.
     */
    private MatcherUri getMatcherUriFor(String pathAndQuery) {

        final MatcherUri newCandidate = new MatcherUri(mAuthority, pathAndQuery);

        NavigableSet<MatcherUri> matchingUris = Sets.filter(mMactherUris, new Predicate<MatcherUri>() {

            @Override
            public boolean apply(MatcherUri input) {

                return newCandidate.equals(input);
            }
        });

        if (matchingUris.size() == 0) {

            newCandidate.setId(++mMatcherUriIdCount);
            mMactherUris.add(newCandidate);

            return newCandidate;
        }

        return matchingUris.first();
    }
}