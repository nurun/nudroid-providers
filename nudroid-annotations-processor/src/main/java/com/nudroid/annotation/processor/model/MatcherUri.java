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

import com.nudroid.annotation.processor.DuplicatePathException;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Represents a URI to be mapped by a UriMatcher.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class MatcherUri {

    private static final String QUERY_STRING_REGEXP = "\\?.*";
    private static final String PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";
    private static final String LEADING_SLASH_REGEXP = "^/";

    private int id;
    private Authority mAuthority;
    private String mPath;
    private boolean mHasQueryStringMatchersOnly = true;

    /* Delegate URIs are sorted by query parameter count */
    private NavigableSet<MethodBinding> mQueryBindings = new TreeSet<>((MethodBinding uri1, MethodBinding uri2) -> {

        if (uri1.getQueryStringParameterCount() == uri2.getQueryStringParameterCount()) {

            return uri1.getNormalizedPath()
                    .compareTo(uri2.getNormalizedPath());
        }

        return uri2.getQueryStringParameterCount() - uri1.getQueryStringParameterCount();
    });

    /* Delegate URIs are sorted by query parameter count */
    private NavigableSet<MethodBinding> mUpdateBindings = new TreeSet<>((MethodBinding uri1, MethodBinding uri2) -> {

        if (uri1.getQueryStringParameterCount() == uri2.getQueryStringParameterCount()) {

            return uri1.getNormalizedPath()
                    .compareTo(uri2.getNormalizedPath());
        }

        return uri2.getQueryStringParameterCount() - uri1.getQueryStringParameterCount();
    });

    /**
     * Creates an instance of this class.
     *
     * @param authority
     *         The authority for this URI.
     * @param path
     *         the uri path, with placeholders
     * @param placeholderTargetTypes
     *         The types of the parameters mapping to the placeholders, in the order they appear.
     */
    //TODO This was a quick fix for a BUG faced on project. Review this and refactor it better. For now,
    // we assume anything which is not a string is a number. Do a proper assessment. Keep a collection of mappings
    // and utilities, map all supported types and their corresponding ParamTypePattern and get from that map,
    // throwing an exception if the type is not supported.
    public MatcherUri(Authority authority, String path, List<UriMatcherPathPatternType> placeholderTargetTypes) {

        String normalizedPath = path.replaceAll(QUERY_STRING_REGEXP, "");

        for (UriMatcherPathPatternType pattern : placeholderTargetTypes) {

            normalizedPath = normalizedPath.replaceFirst(PLACEHOLDER_REGEXP, pattern.getPattern());
        }

        normalizedPath = normalizedPath.replaceAll(LEADING_SLASH_REGEXP, "");

        this.mAuthority = authority;
        this.mPath = normalizedPath;
    }

    /**
     * Gets the set of delegate uris which handles @Query methods. Methods will be ordered by number of query
     * parameters, descending.
     *
     * @return The set of delegate uris which handles @Query methods
     */
    @SuppressWarnings("UnusedDeclaration")
    public NavigableSet<MethodBinding> getQueryBindings() {

        return mQueryBindings;
    }

    /**
     * Gets the set of delegate uris which handles @Update methods.
     *
     * @return The set of delegate uris which handles @Update methods
     */
    @SuppressWarnings("UnusedDeclaration")
    public NavigableSet<MethodBinding> getUpdateBindings() {

        return mUpdateBindings;
    }

    /**
     * Registers a new {@link MethodBinding} for a query method for the provided path and query string. Delegate uris
     * (as opposed to matcher uris) does take the query string into consideration when differentiating between URLs.
     *
     * @param pathAndQuery
     *         The path and query to register as a query delegate uri.
     *
     * @return A new MethodBinding object binding the path and query string combination to the target method.
     *
     * @throws DuplicatePathException
     *         If the path and query string has already been associated with an existing @Query DelegateMethod.
     */
    public MethodBinding registerQueryUri(String pathAndQuery) {

        final MethodBinding candidateMethodBinding = new MethodBinding(mAuthority.getName(), pathAndQuery);

        MethodBinding registeredMethodBinding = findEquivalentQueryMethodBinding(candidateMethodBinding);

        if (registeredMethodBinding != null) {

            throw new DuplicatePathException(registeredMethodBinding.getDelegateMethod()
                    .getExecutableElement());
        }

        mQueryBindings.add(candidateMethodBinding);

        if (candidateMethodBinding.getQueryStringParameterCount() == 0) {

            mHasQueryStringMatchersOnly = false;
        }

        return candidateMethodBinding;
    }

    /**
     * Registers a new {@link MethodBinding} for an update method for the provided path and query string. Delegate uris
     * (as opposed to matcher uris) does take the query string into consideration when differentiating between URLs.
     *
     * @param pathAndQuery
     *         The path and query to register as a query delegate uri.
     *
     * @return A new MethodBinding object binding the path and query string combination to the target method.
     *
     * @throws DuplicatePathException
     *         If the path and query string has already been associated with an existing @Update DelegateMethod.
     */
    public MethodBinding registerUpdateUri(String pathAndQuery) {

        final MethodBinding candidateMethodBinding = new MethodBinding(mAuthority.getName(), pathAndQuery);

        MethodBinding registeredMethodBinding = findEquivalentUpdateMethodBinding(candidateMethodBinding);

        if (registeredMethodBinding != null) {

            throw new DuplicatePathException(registeredMethodBinding.getDelegateMethod()
                    .getExecutableElement());
        }

        mUpdateBindings.add(candidateMethodBinding);

        if (candidateMethodBinding.getQueryStringParameterCount() == 0) {

            mHasQueryStringMatchersOnly = false;
        }

        return candidateMethodBinding;
    }

    /**
     * Gets the id to be mapped to this URI in the <a href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>.
     *
     * @return The id to be mapped to this URI in the <a href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>
     */
    @SuppressWarnings("UnusedDeclaration")
    public int getId() {

        return this.id;
    }

    /**
     * Gets the authority name of this URI.
     *
     * @return The authority name of this URI.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getAuthorityName() {

        return mAuthority.getName();
    }

    /**
     * Gets the path of this URI. The path will already be normalized for a <a href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>
     * (i.e. placeholder names will be replaced by '*').
     *
     * @return The normalized path for this URI.
     */
    public String getNormalizedPath() {

        return mPath;
    }

    /**
     * Check whether or not this matcher uri only matches paths containing query strings.
     *
     * @return <tt>true</tt> if mathcing only paths with query string, <tt>false</tt> otherwise.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean hasQueryStringMatchersOnly() {

        return mHasQueryStringMatchersOnly;
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
        result = prime * result + ((mAuthority == null) ? 0 : mAuthority.hashCode());
        result = prime * result + ((mPath == null) ? 0 : mPath.hashCode());
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
        MatcherUri other = (MatcherUri) obj;
        if (mAuthority == null) {
            if (other.mAuthority != null) return false;
        } else if (!mAuthority.equals(other.mAuthority)) return false;
        if (mPath == null) {
            if (other.mPath != null) return false;
        } else if (!mPath.equals(other.mPath)) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MatcherUri [id=" + id + ", mAuthority=" + mAuthority + ", mPath=" + mPath + "]";
    }

    /**
     * Sets the id of this URI to be mapped to a the id to be mapped to this URI in the <a
     * href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>.
     *
     * @param uriId
     *         The URI id.
     */
    void setId(int uriId) {

        this.id = uriId;
    }

    private MethodBinding findEquivalentQueryMethodBinding(final MethodBinding candidateMethodBinding) {

        List<MethodBinding> matchingMethodBindings = mQueryBindings.stream()
                .filter(candidateMethodBinding::equals)
                .collect(Collectors.toList());

        return matchingMethodBindings.isEmpty() ? null : matchingMethodBindings.get(0);
    }

    private MethodBinding findEquivalentUpdateMethodBinding(final MethodBinding candidateMethodBinding) {

        List<MethodBinding> matchingMethodBindings = mUpdateBindings.stream()
                .filter(candidateMethodBinding::equals)
                .collect(Collectors.toList());

        return matchingMethodBindings.isEmpty() ? null : matchingMethodBindings.get(0);
    }
}
