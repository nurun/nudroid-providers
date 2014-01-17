package com.nudroid.annotation.processor.model;

import java.net.URI;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.nudroid.annotation.processor.DuplicatePathException;
import com.nudroid.annotation.processor.IllegalUriPathException;

/**
 * Represents a URI to be mapped by a UriMatcher.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class MatcherUri {

    private static String PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";
    private static String LEADING_SLASH_REGEXP = "^\\/";

    private int id;
    private Authority mAuthority;
    private String mPath;
    private NavigableSet<DelegateUri> mDelegateUris = new TreeSet<DelegateUri>(new Comparator<DelegateUri>() {

        /**
         * 
         * <p/>
         * {@inheritDoc}
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(DelegateUri o1, DelegateUri o2) {
            return o2.getQueryStringParameterCount() - o1.getQueryStringParameterCount();
        }
    });

    public NavigableSet<DelegateUri> getDelegateUris() {

        return mDelegateUris;
    }

    public boolean hasQueryMethods() {

        NavigableSet<DelegateUri> queryDelegateUri = Sets.filter(mDelegateUris, new Predicate<DelegateUri>() {

            @Override
            public boolean apply(DelegateUri input) {

                return input.getQueryDelegateMethod() != null;
            }
        });

        return queryDelegateUri.size() > 0;
    }

    /**
     * Creates an instance of this class.
     * 
     * @param authority
     *            The authority for this URI.
     * @param path
     *            The mapped URI path.
     */
    public MatcherUri(Authority authority, String path) {

        String normalizedPath = path.replaceAll(PLACEHOLDER_REGEXP, "*").replaceAll(LEADING_SLASH_REGEXP, "");
        URI uri;

        try {
            uri = URI.create(String.format("content://%s/%s", authority.getName(), normalizedPath));
        } catch (IllegalArgumentException e) {
            throw new IllegalUriPathException(e);
        }

        this.mAuthority = authority;
        this.mPath = uri.getPath();
    }

    /**
     * Registers a new {@link DelegateUri} for a query method for the provided path and query string. Delegate uris (as
     * opposed to matcher uris) does take the query string into consideration when differentiating between URLs.
     * 
     * @param pathAndQuery
     *            The path and query to register as a query delegate uri.
     * 
     * @return A new DelegateUri for the path and query.
     * 
     * @throws DuplicatePathException
     *             If the path and query string has already been associated with an existing DelegateMethod.
     */
    public DelegateUri registerQueryDelegateUri(String pathAndQuery) {

        final DelegateUri candidateDelegateUri = new DelegateUri(this, pathAndQuery);

        DelegateUri registeredDelegateUri = findEquivalentDelegateUri(candidateDelegateUri);

        if (registeredDelegateUri != null) {

            throw new DuplicatePathException(registeredDelegateUri.getQueryDelegateMethod().getExecutableElement());
        }

        mDelegateUris.add(candidateDelegateUri);

        return candidateDelegateUri;
    }

    private DelegateUri findEquivalentDelegateUri(final DelegateUri candidateDelegateUri) {

        NavigableSet<DelegateUri> matchingDelegateUris = Sets.filter(mDelegateUris, new Predicate<DelegateUri>() {

            /*
             * Checks if the path and query has already been registered in the delegate uri set.
             */
            @Override
            public boolean apply(DelegateUri input) {

                return candidateDelegateUri.equals(input) && input.getQueryDelegateMethod() != null;
            }
        });

        return matchingDelegateUris.isEmpty() ? null : matchingDelegateUris.first();
    }

    /**
     * Gets the id to be mapped to this URI in the <a
     * href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>.
     * 
     * @return The id to be mapped to this URI in the <a
     *         href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>
     */
    public int getId() {

        return this.id;
    }

    /**
     * Gets the authority name of this URI.
     * 
     * @return The authority name of this URI.
     */
    public String getAuthorityName() {

        return mAuthority.getName();
    }

    /**
     * Gets the path of this URI. The path will already be normalized for a <a
     * href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a> (i.e. placeholder
     * names will be replaced by '*').
     * 
     * @return The normalized path for this URI.
     */
    public String getNormalizedPath() {

        return mPath;
    }

    /**
     * Sets the id of this URI to be mapped to a the id to be mapped to this URI in the <a
     * href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a>.
     * 
     * @param uriId
     *            The URI id.
     */
    void setId(int uriId) {

        this.id = uriId;
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MatcherUri other = (MatcherUri) obj;
        if (mAuthority == null) {
            if (other.mAuthority != null)
                return false;
        } else if (!mAuthority.equals(other.mAuthority))
            return false;
        if (mPath == null) {
            if (other.mPath != null)
                return false;
        } else if (!mPath.equals(other.mPath))
            return false;
        return true;
    }

    /**
     * 
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MatcherUri [id=" + id + ", mAuthority=" + mAuthority + ", mPath=" + mPath + "]";
    }
}
