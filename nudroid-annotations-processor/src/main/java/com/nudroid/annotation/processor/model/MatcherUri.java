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
import com.nudroid.annotation.processor.UsedBy;

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
    @UsedBy({"RouterTemplateQuery.stg"})
    public NavigableSet<UriToMethodBinding> getQueryBindings() {

        return queryBindings;
    }

    /**
     * Gets the set of delegate uris which handles @Update methods.
     *
     * @return The set of delegate uris which handles @Update methods
     */
    @UsedBy({"RouterTemplateQuery.stg"})
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
     * Gets the id to be mapped to this URI in the <a href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>.
     *
     * @return The id to be mapped to this URI in the <a href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>
     */
    @UsedBy({"RouterTemplate.stg", "RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public int getId() {

        return this.id;
    }

    /**
     * Gets the authority name of this URI.
     *
     * @return The authority name of this URI.
     */
    @UsedBy("RouterTemplate.stg")
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
    //TODO review the approach for binding uris to methods using query strings
    //TODO see if this method is required. it' name bother me a lot
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
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
}
