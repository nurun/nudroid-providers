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

import com.nudroid.annotation.processor.LoggingUtils;
import com.nudroid.annotation.processor.ProcessorUtils;
import com.nudroid.annotation.processor.UsedBy;
import com.nudroid.annotation.processor.ValidationErrorGatherer;

import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a URI to be mapped by a UriMatcher.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class MatcherUri {

    private int id;
    private Authority authority;
    private String path;
    private boolean hasQueryStringMatchersOnly = true;

    /* Bindings are sorted by query parameter count */
    private Comparator<UriToMethodBinding> bindingComparator = new Comparator<UriToMethodBinding>() {
        @Override
        public int compare(UriToMethodBinding binding1, UriToMethodBinding binding2) {

            /* If same number of query parameters, order is unimportant since equivalent bindings (i.e. same path and
             same query string declarations) are flagged as errors */
            if (binding1.getQueryStringParameterCount() == binding2.getQueryStringParameterCount()) {

                return -1;
            }

            return binding2.getQueryStringParameterCount() - binding1.getQueryStringParameterCount();
        }
    };

    private final NavigableSet<UriToMethodBinding> queryBindings = new TreeSet<>(bindingComparator);
    private final NavigableSet<UriToMethodBinding> updateBindings = new TreeSet<>(bindingComparator);

    private MatcherUri() {

    }

    /**
     * Gets the set of delegate uris which handles @Query methods. Methods will be ordered by number of query
     * parameters, descending.
     *
     * @return the set of delegate uris which handles @Query methods
     */
    @UsedBy({"RouterTemplateQuery.stg"})
    public NavigableSet<UriToMethodBinding> getQueryBindings() {

        return queryBindings;
    }

    /**
     * Gets the set of delegate uris which handles @Update methods.
     *
     * @return he set of delegate uris which handles @Update methods
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
    public void registerBindingForQuery(UriToMethodBinding uriToMethodBinding,
                                        Consumer<ValidationErrorGatherer> errorCallback) {

        ValidationErrorGatherer gatherer = new ValidationErrorGatherer();

        UriToMethodBinding registeredUriToMethodBinding = findEquivalentQueryMethodBinding(uriToMethodBinding);

        if (registeredUriToMethodBinding != null) {

            gatherer.gatherError(String.format("An equivalent binding has already been registered by method '%s'",
                    registeredUriToMethodBinding.getDelegateMethod()
                            .getExecutableElement()), uriToMethodBinding.getDelegateMethod()
                    .getExecutableElement(), LoggingUtils.LogLevel.ERROR);

            errorCallback.accept(gatherer);
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
     * @return the id to be mapped to this URI in the <a href="http://developer.android
     * .com/reference/android/content/UriMatcher.html">UriMatcher</a>
     */
    @UsedBy({"RouterTemplate.stg", "RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public int getId() {

        return this.id;
    }

    /**
     * Gets the authority name of this URI.
     *
     * @return the authority name of this URI
     */
    @UsedBy("RouterTemplate.stg")
    public String getAuthorityName() {

        return authority.getName();
    }

    /**
     * Gets the path of this URI. The path will already be normalized for a <a href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>
     * (i.e. placeholder names will be replaced by '*').
     *
     * @return the normalized path for this URI
     */
    public String getNormalizedPath() {

        return path;
    }

    /**
     * Check whether or not this matcher uri only matches paths containing query strings.
     *
     * @return <tt>true</tt> if matching only paths with query string, <tt>false</tt> otherwise.
     */
    //TODO see if this method is required. it's name bother me a lot
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

    private UriToMethodBinding findEquivalentQueryMethodBinding(final UriToMethodBinding candidateUriToMethodBinding) {

        List<UriToMethodBinding> matchingUriToMethodBindings = queryBindings.stream()
                .filter(candidateUriToMethodBinding::equals)
                .collect(Collectors.toList());

        return matchingUriToMethodBindings.isEmpty() ? null : matchingUriToMethodBindings.get(0);
    }

    /**
     * Builder for Interceptor.
     */
    public static class Builder implements ModelBuilder<MatcherUri> {

        private final Authority authority;
        private final String path;
        private static int matcherUriIdCount = 0;

        /**
         * Initializes the builder.
         *
         * @param authority
         *         the authority for this URI
         * @param path
         *         the uri path, with UriMatcher wildcards
         */
        public Builder(Authority authority, String path) {

            this.authority = authority;
            this.path = path;
        }

        /**
         * Builds an instance of the InterceptorPoint class.
         * <p>
         * {@inheritDoc}
         */
        public MatcherUri build(ProcessorUtils processorUtils, Consumer<ValidationErrorGatherer> errorCallback) {

            MatcherUri matcherUri = new MatcherUri();

            matcherUri.authority = this.authority;
            matcherUri.path = this.path;
            matcherUri.id = ++matcherUriIdCount;

            return matcherUri;
        }
    }
}
