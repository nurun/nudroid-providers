package com.nudroid.annotation.processor.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nudroid.annotation.processor.DuplicateUriPlaceholderException;
import com.nudroid.annotation.processor.IllegalUriPathException;

/**
 * A uniquely mapped URI tied to a delegate method. This class extends the concept of a matcher URI adding query string
 * variables to this URIs identification.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateUri {

    private static String PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";
    private static String LEADING_SLASH_REGEXP = "^\\/";

    private int mId;
    private String mAuthority;
    private String mPath;
    private String queryString;
    private List<PathPlaceholderParameter> placeholders = new ArrayList<PathPlaceholderParameter>();
    private List<PathPlaceholderParameter> pathPlaceholders = new ArrayList<PathPlaceholderParameter>();
    private List<PathPlaceholderParameter> queryPlaceholders = new ArrayList<PathPlaceholderParameter>();
    private Set<String> queryParameterNames = new HashSet<String>();

    private String originalPathAndQuery;

    /**
     * Creates an instance of this class.
     * 
     * @param matcherUri
     *            The matcher uri for this method.
     * @param pathAndQuery
     *            The path and optional query string.
     */
    public DelegateUri(MatcherUri matcherUri, String pathAndQuery) {

        this.originalPathAndQuery = pathAndQuery;

        String normalizedPath = pathAndQuery.replaceAll(PLACEHOLDER_REGEXP, "*").replaceAll(LEADING_SLASH_REGEXP, "");
        parsePlaceholders(normalizedPath);
        URI uri;

        try {
            uri = URI.create(String.format("content://%s/%s", matcherUri.getAuthorityName(), normalizedPath));
        } catch (IllegalArgumentException e) {
            throw new IllegalUriPathException(e);
        }

        this.mAuthority = matcherUri.getAuthorityName();
        this.mPath = uri.getPath();
        this.queryString = uri.getQuery();
        this.mId = matcherUri.getId();
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
     * Checks if this URI has the provided named path placeholder.
     * 
     * @param parameterName
     *            The name of the placeholder to check.
     * 
     * @return <tt>true</tt> if this URI has a path placeholder named as parameterName, <tt>false</tt> otherwise.
     */
    public boolean containsPathPlaceholder(String parameterName) {

        PathPlaceholderParameter pathParam = new PathPlaceholderParameter(parameterName, 0);

        return pathPlaceholders.contains(pathParam);
    }

    /**
     * Checks if this URI has the provided named query string placeholder.
     * 
     * @param parameterName
     *            The name of the placeholder to check.
     * 
     * @return <tt>true</tt> if this URI has a query string placeholder named as parameterName, <tt>false</tt>
     *         otherwise.
     */
    public boolean containsQueryPlaceholder(String parameterName) {

        PathPlaceholderParameter pathParam = new PathPlaceholderParameter(parameterName, 0);

        return queryPlaceholders.contains(pathParam);
    }

    /**
     * Gets a string representation of the position this named parameter appears in this URI's path.
     * 
     * @param name
     *            The name of the path placeholder to check.
     * 
     * @return The position this named parameter appears in this URI's path.
     * 
     * @throws NullPointerException
     *             if this URI does not have the provided path parameter.
     */
    public String getPathParameterPosition(String name) {

        PathPlaceholderParameter pathParam = new PathPlaceholderParameter(name, 0);

        return pathPlaceholders.get(pathPlaceholders.indexOf(pathParam)).getKey();
    }

    /**
     * Gets a string representation of the name of the query string parameter name this parameter appears in this URI's
     * query string.
     * 
     * @param name
     *            The name of the path placeholder to check.
     * 
     * @return The query string parameter name this parameter appears in this URI's query string.
     * 
     * @throws NullPointerException
     *             if this URI does not have the provided query string parameter.
     */
    public String getQueryParameterPlaceholderName(String name) {

        PathPlaceholderParameter queryParam = new PathPlaceholderParameter(name, 0);
        return queryPlaceholders.get(queryPlaceholders.indexOf(queryParam)).getKey();
    }

    /**
     * Gets the list o query parameter names for this URI.
     * 
     * @return The list o query parameter names for this URI.
     */
    public Set<String> getQueryParameterNames() {

        return Collections.unmodifiableSet(queryParameterNames);
    }

    private void parsePlaceholders(String pathAndQuery) {

        Pattern placeholderPattern = Pattern.compile(PLACEHOLDER_REGEXP);

        String[] pathAndQueryString = pathAndQuery.split("\\?");

        if (pathAndQueryString.length > 2) {

            throw new IllegalUriPathException(String.format("The path '%s' is invalid.", pathAndQuery));
        }

        if (pathAndQueryString.length >= 1) {

            String pathSection = pathAndQueryString[0];

            String[] pathElements = pathSection.split("/");

            for (int position = 0; position < pathElements.length; position++) {

                Matcher m = placeholderPattern.matcher(pathElements[position]);

                if (m.find()) {

                    String paramName = m.group(1);
                    addPathPlaceholder(paramName, position);
                }
            }
        }

        if (pathAndQueryString.length == 2) {

            String querySection = pathAndQueryString[1];
            querySection = querySection.replaceAll("^\\?+", "");
            querySection = querySection.replaceAll("^\\&+", "");

            String[] queryVars = querySection.split("\\&");

            for (int position = 0; position < queryVars.length; position++) {

                String[] nameAndValue = queryVars[position].split("\\=");

                if (nameAndValue.length != 2) {

                    throw new IllegalUriPathException(String.format("Segment '%s' on path %s is invalid.",
                            queryVars[position], originalPathAndQuery));
                }

                queryParameterNames.add(nameAndValue[0]);

                Matcher m = placeholderPattern.matcher(nameAndValue[1]);

                if (m.matches()) {

                    String placeholderName = m.group(1);
                    addQueryPlaceholder(placeholderName, nameAndValue[0]);
                }
            }
        }
    }

    private void addPathPlaceholder(String placeholderName, int position) {

        PathPlaceholderParameter pathPlaceholder = new PathPlaceholderParameter(placeholderName, position);

        if (placeholders.contains(pathPlaceholder)) {

            throw new DuplicateUriPlaceholderException(placeholderName, placeholders.get(
                    placeholders.indexOf(pathPlaceholder)).getKey(), Integer.toString(position));
        }

        placeholders.add(pathPlaceholder);
        pathPlaceholders.add(pathPlaceholder);
    }

    private void addQueryPlaceholder(String placeholderName, String queryParameterName) {

        PathPlaceholderParameter queryPlaceholder = new PathPlaceholderParameter(placeholderName, queryParameterName);

        if (placeholders.contains(queryPlaceholder)) {

            throw new DuplicateUriPlaceholderException(placeholderName, placeholders.get(
                    placeholders.indexOf(queryPlaceholder)).getKey(), queryParameterName);
        }

        placeholders.add(queryPlaceholder);
        queryPlaceholders.add(queryPlaceholder);
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
        result = prime * result + ((queryParameterNames == null) ? 0 : queryParameterNames.hashCode());
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
        DelegateUri other = (DelegateUri) obj;
        if (mAuthority == null) {
            if (other.mAuthority != null) return false;
        } else if (!mAuthority.equals(other.mAuthority)) return false;
        if (mPath == null) {
            if (other.mPath != null) return false;
        } else if (!mPath.equals(other.mPath)) return false;
        if (queryParameterNames == null) {
            if (other.queryParameterNames != null) return false;
        } else if (!queryParameterNames.equals(other.queryParameterNames)) return false;
        return true;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Uri [authority=" + mAuthority + ", path=" + mPath + ", queryString=" + queryString
                + ", pathPlaceholders=" + pathPlaceholders + ", queryPlaceholders=" + queryPlaceholders + "]";
    }

    public int getmId() {
        return mId;
    }
}
