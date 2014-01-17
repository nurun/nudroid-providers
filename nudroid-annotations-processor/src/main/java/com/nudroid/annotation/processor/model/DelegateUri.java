package com.nudroid.annotation.processor.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.ExecutableElement;

import com.nudroid.annotation.processor.DuplicateUriPlaceholderException;
import com.nudroid.annotation.processor.IllegalUriPathException;

/**
 * A uniquely mapped URI tied to a delegate method. This class extends the concept of a {@link MatcherUri} by adding
 * relevance to query string parameters.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateUri {

    private static final String PLACEHOLDER_WILDCARD = "*";
    private static final String AMPERSAND = "\\&";
    private static final String LEADING_AMPERSANDS = "^\\&+";
    private static final String INTERROGATION_MARK = "\\?";
    private static final String LEADING_INTERROGATION_MARKS = "^\\?+";
    private static final String SLASH = "/";

    private static final String LEADING_SLASH = "^\\/";
    private static final String EMPTY_STRING = "";
    private static final String EQUALS_SIGN = "\\=";

    private static final String PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";

    private int mId;
    private String mAuthority;
    private String mPath;
    private String queryString;
    private Map<String, UriPlaceholderParameter> placeholders = new HashMap<String, UriPlaceholderParameter>();
    private Map<String, String> queryParameterNamesAndValues = new HashMap<String, String>();
    private DelegateMethod queryDelegateMethod;

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

        parsePlaceholders(pathAndQuery);
        String normalizedPath = pathAndQuery.replaceAll(PLACEHOLDER_REGEXP, PLACEHOLDER_WILDCARD).replaceAll(
                LEADING_SLASH, EMPTY_STRING);
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
     * Gets this delegate URI's id, as expected by a UrlMatcher.
     * 
     * @return This delegate URI's id.
     */
    public int getId() {
        return mId;
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
    public boolean containsPlaceholder(String parameterName) {

        return placeholders.containsKey(parameterName);
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
    public String getParameterPosition(String name) {

        UriPlaceholderParameter placeholder = placeholders.get(name);

        return placeholder != null ? placeholder.getKey() : null;
    }

    /**
     * Gets the list o query parameter names for this URI.
     * 
     * @return The list o query parameter names for this URI.
     */
    public Map<String, String> getQueryParameterNamesAndValues() {

        return queryParameterNamesAndValues;
    }

    /**
     * Gets the number of query string parameters in this URI.
     * 
     * @return The number of query string parameters in this URI.
     */
    public int getQueryStringParameterCount() {

        return queryParameterNamesAndValues.size();
    }

    /**
     * Gets the placeholder type associated with the given placeholder name.
     * 
     * @param placeholderName
     *            The name of the placeholder.
     * 
     * @return The type of placeholder associated to the given placeholder name.
     */
    public UriPlaceholderType getUriPlaceholderType(String placeholderName) {

        return placeholders.get(placeholderName).getUriPlaceholderType();
    }

    private void parsePlaceholders(String pathAndQuery) {

        Pattern placeholderPattern = Pattern.compile(PLACEHOLDER_REGEXP);

        String[] pathAndQueryString = pathAndQuery.split(INTERROGATION_MARK);

        if (pathAndQueryString.length > 2) {

            throw new IllegalUriPathException(String.format("The path '%s' is invalid.", pathAndQuery));
        }

        if (pathAndQueryString.length >= 1) {

            String pathSection = pathAndQueryString[0];
            pathSection = pathSection.replaceAll(LEADING_SLASH, EMPTY_STRING);

            String[] pathElements = pathSection.split(SLASH);

            for (int position = 0; position < pathElements.length; position++) {

                Matcher m = placeholderPattern.matcher(pathElements[position]);

                if (m.find()) {

                    String placeholderName = m.group(1);
                    addPathPlaceholder(placeholderName, position);
                }
            }
        }

        if (pathAndQueryString.length == 2) {

            String querySection = pathAndQueryString[1];

            querySection = querySection.replaceAll(LEADING_INTERROGATION_MARKS, EMPTY_STRING);
            querySection = querySection.replaceAll(LEADING_AMPERSANDS, EMPTY_STRING);

            String[] queryVars = querySection.split(AMPERSAND);

            for (int position = 0; position < queryVars.length; position++) {

                String[] nameAndValue = queryVars[position].split(EQUALS_SIGN);

                if (nameAndValue.length != 2) {

                    throw new IllegalUriPathException(String.format("Segment '%s' on path %s is invalid.",
                            queryVars[position], originalPathAndQuery));
                }

                Matcher m = placeholderPattern.matcher(nameAndValue[1]);

                if (m.matches()) {

                    queryParameterNamesAndValues.put(nameAndValue[0], PLACEHOLDER_WILDCARD);
                    String placeholderName = m.group(1);
                    addQueryPlaceholder(placeholderName, nameAndValue[0]);
                } else {
                    queryParameterNamesAndValues.put(nameAndValue[0], nameAndValue[1]);
                }
            }
        }
    }

    private void addPathPlaceholder(String placeholderName, int position) {

        if (placeholders.containsKey(placeholderName)) {

            throw new DuplicateUriPlaceholderException(placeholderName, placeholders.get(placeholderName).getKey(),
                    Integer.toString(position));
        }

        UriPlaceholderParameter pathPlaceholder = new UriPlaceholderParameter(placeholderName, position);
        placeholders.put(placeholderName, pathPlaceholder);
    }

    private void addQueryPlaceholder(String placeholderName, String queryParameterName) {

        if (placeholders.containsKey(placeholderName)) {

            throw new DuplicateUriPlaceholderException(placeholderName, placeholders.get(placeholderName).getKey(),
                    queryParameterName);
        }

        UriPlaceholderParameter queryPlaceholder = new UriPlaceholderParameter(placeholderName, queryParameterName);
        placeholders.put(placeholderName, queryPlaceholder);
    }

    /**
     * 
     * <p/>
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
        result = prime * result
                + ((queryParameterNamesAndValues == null) ? 0 : queryParameterNamesAndValues.hashCode());
        return result;
    }

    /**
     * 
     * <p/>
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
        DelegateUri other = (DelegateUri) obj;
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
        if (queryParameterNamesAndValues == null) {
            if (other.queryParameterNamesAndValues != null)
                return false;
        } else if (!queryParameterNamesAndValues.equals(other.queryParameterNamesAndValues))
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
        return "DelegateUri [mId=" + mId + ", mAuthority=" + mAuthority + ", mPath=" + mPath + ", queryString="
                + queryString + "]";
    }

    /**
     * Gets the delegate method for a query operation.
     * 
     * @return The DelegateMethod for the query operation or null if this URI does not answers to query requests.
     */
    public DelegateMethod getQueryDelegateMethod() {

        return queryDelegateMethod;
    }

    /**
     * Creates a new DelegateMethod instance and registers it as a query delegate. Overrides any previously set query
     * DelegateMethod for this URI.
     * 
     * @param queryMethod
     *            The ExecutableElement for the method in the delegate class which will answer for queries against this
     *            URI.
     * 
     * @return The newly create and registered query DelegateMethod.
     */
    public DelegateMethod setQueryDelegateMethod(ExecutableElement queryMethod) {

        DelegateMethod delegateMethod = new DelegateMethod(queryMethod, this);
        delegateMethod.setQueryParameterNames(this.getQueryParameterNamesAndValues().keySet());
        
        queryDelegateMethod = delegateMethod;

        return delegateMethod;
    }
}
