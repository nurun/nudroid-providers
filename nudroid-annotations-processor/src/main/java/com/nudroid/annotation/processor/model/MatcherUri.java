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

    private static final String PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";
    private static final String LEADING_SLASH_REGEXP = "^/";

    private int id;
    private final Authority authority;
    private final String path;
    private boolean hasQueryStringMatchersOnly = true;

    /* Delegate URIs are sorted by query parameter count */
    private final NavigableSet<UriToMethodBinding> queryBindings =
            new TreeSet<>((UriToMethodBinding uri1, UriToMethodBinding uri2) -> {

                if (uri1.getQueryStringParameterCount() == uri2.getQueryStringParameterCount()) {

                    return uri1.getPath()
                            .compareTo(uri2.getPath());
                }

                return uri2.getQueryStringParameterCount() - uri1.getQueryStringParameterCount();
            });

    /* Delegate URIs are sorted by query parameter count */
    private final NavigableSet<UriToMethodBinding> updateBindings =
            new TreeSet<>((UriToMethodBinding uri1, UriToMethodBinding uri2) -> {

                if (uri1.getQueryStringParameterCount() == uri2.getQueryStringParameterCount()) {

                    return uri1.getPath()
                            .compareTo(uri2.getPath());
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

        String normalizedPath = path;

        for (UriMatcherPathPatternType pattern : placeholderTargetTypes) {

            normalizedPath = normalizedPath.replaceFirst(PLACEHOLDER_REGEXP, pattern.getPattern());
        }

        normalizedPath = normalizedPath.replaceAll(LEADING_SLASH_REGEXP, "");

        this.authority = authority;
        this.path = normalizedPath;
    }

    /**
     * Creates an instance of this class.
     *
     * @param authority
     *         the authority for this URI
     * @param path
     *         the uri path, with UriMatcher wildcards
     */
    public MatcherUri(Authority authority, String path) {

        this.authority = authority;
        this.path = path;
    }

    /**
     * Gets the set of delegate uris which handles @Query methods. Methods will be ordered by number of query
     * parameters, descending.
     *
     * @return The set of delegate uris which handles @Query methods
     */
    @SuppressWarnings("UnusedDeclaration")
    public NavigableSet<UriToMethodBinding> getQueryBindings() {

        return queryBindings;
    }

    /**
     * Gets the set of delegate uris which handles @Update methods.
     *
     * @return The set of delegate uris which handles @Update methods
     */
    @SuppressWarnings("UnusedDeclaration")
    public NavigableSet<UriToMethodBinding> getUpdateBindings() {

        return updateBindings;
    }

    /**
     * Registers a uri to method binding.
     *
     * @param uriToMethodBinding
     *         the binding to register
     */
    public void registerBindingForQuery(UriToMethodBinding uriToMethodBinding, List<ValidationError> errorAccumulator) {

        UriToMethodBinding registeredUriToMethodBinding = findEquivalentQueryMethodBinding(uriToMethodBinding);

        if (registeredUriToMethodBinding != null) {

            ValidationError error = new ValidationError(
                    String.format("An equivalent path has already been registered by method '%s'",
                            registeredUriToMethodBinding.getDelegateMethod()
                                    .getExecutableElement()), uriToMethodBinding.getDelegateMethod()
                    .getExecutableElement());

            errorAccumulator.add(error);

            return;
        }

        queryBindings.add(uriToMethodBinding);

        if (uriToMethodBinding.getQueryStringParameterCount() == 0) {

            hasQueryStringMatchersOnly = false;
        }
    }

    /**
     * Registers a new {@link UriToMethodBinding} for an update method for the provided path and query string. Delegate
     * uris (as opposed to matcher uris) does take the query string into consideration when differentiating between
     * URLs.
     *
     * @param pathAndQuery
     *         The path and query to register as a query delegate uri.
     *
     * @return A new UriToMethodBinding object binding the path and query string combination to the target method.
     *
     * @throws DuplicatePathException
     *         If the path and query string has already been associated with an existing @Update DelegateMethod.
     */
    public UriToMethodBinding registerUpdateUri(String pathAndQuery) {

//        final UriToMethodBinding candidateUriToMethodBinding =
//                new UriToMethodBinding(authority.getName(), pathAndQuery);
//
//        UriToMethodBinding registeredUriToMethodBinding =
//                findEquivalentUpdateMethodBinding(candidateUriToMethodBinding);
//
//        if (registeredUriToMethodBinding != null) {
//
//            throw new DuplicatePathException(registeredUriToMethodBinding.getDelegateMethod()
//                    .getExecutableElement());
//        }
//
//        updateBindings.add(candidateUriToMethodBinding);
//
//        if (candidateUriToMethodBinding.getQueryStringParameterCount() == 0) {
//
//            hasQueryStringMatchersOnly = false;
//        }
//
//        return candidateUriToMethodBinding;

        return null;
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

        return authority.getName();
    }

    /**
     * Gets the path of this URI. The path will already be normalized for a <a href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>
     * (i.e. placeholder names will be replaced by '*').
     *
     * @return The normalized path for this URI.
     */
    public String getNormalizedPath() {

        return path;
    }

    /**
     * Check whether or not this matcher uri only matches paths containing query strings.
     *
     * @return <tt>true</tt> if matching only paths with query string, <tt>false</tt> otherwise.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean hasQueryStringMatchersOnly() {

        return hasQueryStringMatchersOnly;
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
        result = prime * result + ((authority == null) ? 0 : authority.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        if (authority == null) {
            if (other.authority != null) return false;
        } else if (!authority.equals(other.authority)) return false;
        if (path == null) {
            if (other.path != null) return false;
        } else if (!path.equals(other.path)) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MatcherUri [id=" + id + ", authority=" + authority + ", path=" + path + "]";
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

    private UriToMethodBinding findEquivalentQueryMethodBinding(final UriToMethodBinding candidateUriToMethodBinding) {

        List<UriToMethodBinding> matchingUriToMethodBindings = queryBindings.stream()
                .filter(candidateUriToMethodBinding::equals)
                .collect(Collectors.toList());

        return matchingUriToMethodBindings.isEmpty() ? null : matchingUriToMethodBindings.get(0);
    }

    private UriToMethodBinding findEquivalentUpdateMethodBinding(final UriToMethodBinding candidateUriToMethodBinding) {

        List<UriToMethodBinding> matchingUriToMethodBindings = updateBindings.stream()
                .filter(candidateUriToMethodBinding::equals)
                .collect(Collectors.toList());

        return matchingUriToMethodBindings.isEmpty() ? null : matchingUriToMethodBindings.get(0);
    }

//    /**
//     * Builder pattern.
//     */
//    public static class Builder {
//        public MatcherUri build() {
//        }
//    }
}
