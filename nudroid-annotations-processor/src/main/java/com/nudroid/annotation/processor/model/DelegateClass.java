/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.nudroid.annotation.processor.model;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

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

    //TODO Check if there's a more performing way of doing this check.
    /* UriMatcher has an undocumented matching algorithm. It will match the first path (in the order they have been
     * added) and ignore the rest of the entries even if the selected match fail later on. For example:
     *
     *     * / *
     *     page / * / *
     *
     * will not match 'page/section/33'. 'page' will match the first '*' so UriMatcher will try to use that entry to
     * validate the path. Since the path has three segments while the pattern only has 2, it will fail BUT IT WILL NOT
     * TRY TO MATCH against THE NEXT (and correct) PATTERN.
     *
     * However, if we do so:
     *
     *     page / * / *
     *     * / *
     *
     * Then it will match since the first entry will be selected. Basically, it traverses the list of patterns and stops
     * as soon as it finds an entry whose first item match the input string.
     *
     * The sorting algorithm below sorts the entries by listing the explicit patterns (non wildcards) first based
     * on their position in the path segments.
     */
    private NavigableSet<MatcherUri> mMatcherUris = new TreeSet<>((uri1, uri2) -> {

        String[] uri1Paths = uri1.getNormalizedPath()
                .split("/");
        String[] uri2Paths = uri2.getNormalizedPath()
                .split("/");

        for (int i = 0; i < Math.min(uri1Paths.length, uri2Paths.length); i++) {

            if (!uri1Paths[i].equals("*") && uri2Paths[i].equals("*")) {
                return -1;
            }

            if (uri1Paths[i].equals("*") && !uri2Paths[i].equals("*")) {
                return 1;
            }
        }

        int diff = uri2Paths.length - uri1Paths.length;

        /* If the path has the same number of elements, compare each component. */
        if (diff == 0) {

            for (int i = 0; i < uri1Paths.length; i++) {

                diff = uri1Paths[i].compareTo(uri2Paths[i]);

                if (diff != 0) {
                    return diff;
                }
            }
        }

        return diff;
    });

    /**
     * Creates an instance of this class.
     *
     * @param authorityName
     *         The authority name being handled by the delegate class.
     * @param typeElement
     *         The {@link TypeElement} for the delegate class as provided by the round environment.
     */
    public DelegateClass(String authorityName, TypeElement typeElement) {

        this.mTypeElement = typeElement;
        this.mQualifiedName = typeElement.toString();
        this.mSimpleName = typeElement.getSimpleName()
                .toString();
        this.mAuthority = new Authority(authorityName);

        String baseName = this.mSimpleName;
        baseName = baseName.replaceAll("(?i)Delegate", "")
                .replaceAll("(?i)ContentProvider", "");

        StringBuilder providerSimpleName = new StringBuilder(baseName);
        StringBuilder routerSimpleName = new StringBuilder(this.mSimpleName);
        Element parentElement = typeElement.getEnclosingElement();

        while (parentElement != null && !parentElement.getKind()
                .equals(ElementKind.PACKAGE)) {

            providerSimpleName.insert(0, "$")
                    .insert(0, parentElement.getSimpleName());
            routerSimpleName.insert(0, "$")
                    .insert(0, parentElement.getSimpleName());
            parentElement = parentElement.getEnclosingElement();
        }

        providerSimpleName.append("ContentProvider");
        routerSimpleName.append("Router");

        if (parentElement != null && parentElement.getKind()
                .equals(ElementKind.PACKAGE)) {

            this.mBasePackageName = ((PackageElement) parentElement).getQualifiedName()
                    .toString();
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
     *         The path and query string to register.
     * @param placeholderTargetTypes
     *         The types of the parameters mapping to the placeholders, in the order they appear.
     *
     * @return A new DelegateUri object representing this path and query string combination.
     *
     * @throws DuplicatePathException
     *         If the path and query string has already been associated with an existing @Query DelegateMethod.
     */
    public DelegateUri registerPathForQuery(String pathAndQuery, List<ParamTypePattern> placeholderTargetTypes) {

        MatcherUri matcherUri = getMatcherUriFor(pathAndQuery, placeholderTargetTypes);
        DelegateUri delegateUri = matcherUri.registerQueryDelegateUri(pathAndQuery);

        return delegateUri;
    }

    /**
     * Registers a @Update path and query string to be handled by this delegate class. DelegateUris, as opposed to
     * MatcherUri does take query string in consideration when differentiating between URIs.
     *
     * @param pathAndQuery
     *         The path and query string to register.
     * @param placeholderTargetTypes
     *         The types of the parameters mapping to the placeholders, in the order they appear.
     *
     * @return A new DelegateUri object representing this path and query string combination.
     *
     * @throws DuplicatePathException
     *         If the path and query string has already been associated with an existing @Update DelegateMethod.
     */
    public DelegateUri registerPathForUpdate(String pathAndQuery, List<ParamTypePattern> placeholderTargetTypes) {

        MatcherUri matcherUri = getMatcherUriFor(pathAndQuery, placeholderTargetTypes);
        DelegateUri delegateUri = matcherUri.registerUpdateDelegateUri(pathAndQuery);

        return delegateUri;
    }

    /**
     * Sets if the delegate class implements the delegate interface.
     *
     * @param doesImplementDelegateInterface
     *         <tt>true</tt> if the delegate class implements the {@link ContentProvider} interface, <tt>false</tt>
     *         otherwise.
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
     * without the package name).
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
     * otherwise.
     */
    public boolean hasContentProviderDelegateInterface() {

        return mHasImplementedDelegateInterface;
    }

    /**
     * Gets the {@link MatcherUri}s this delegate class accepts.
     *
     * @return The {@link MatcherUri}s this delegate class accepts.
     */
    public NavigableSet<MatcherUri> getMatcherUris() {

        return mMatcherUris;
    }

    /**
     * Checks if a {@link MatcherUri} has already been created for the path and query. If yes, returns that instance. If
     * not, a new one is created and registered.
     *
     * @param pathAndQuery
     *         The path and query to check.
     * @param placeholderTargetTypes
     *         The types of the parameters mapping to the placeholders, in the order they appear.
     *
     * @return The {@link MatcherUri} for the path and query.
     */
    private MatcherUri getMatcherUriFor(String pathAndQuery, List<ParamTypePattern> placeholderTargetTypes) {

        final MatcherUri newCandidate = new MatcherUri(mAuthority, pathAndQuery, placeholderTargetTypes);

        NavigableSet<MatcherUri> matchingUris = Sets.filter(mMatcherUris, input -> newCandidate.equals(input));

        if (matchingUris.size() == 0) {

            newCandidate.setId(++mMatcherUriIdCount);
            mMatcherUris.add(newCandidate);

            return newCandidate;
        }

        return matchingUris.first();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mQualifiedName == null) ? 0 : mQualifiedName.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DelegateClass other = (DelegateClass) obj;
        if (mQualifiedName == null) {
            if (other.mQualifiedName != null) return false;
        } else if (!mQualifiedName.equals(other.mQualifiedName)) return false;
        return true;
    }

}