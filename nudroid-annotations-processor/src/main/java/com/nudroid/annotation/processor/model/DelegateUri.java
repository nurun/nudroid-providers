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

    private String mAuthority;
    private String mPath;
    private String mQueryString;
    private Map<String, UriPlaceholderParameter> mPlaceholders = new HashMap<String, UriPlaceholderParameter>();
    private Map<String, String> mQueryStringParameterNamesAndValues = new HashMap<String, String>();
    private DelegateMethod mQueryDelegateMethod;
    private DelegateMethod mUpdateDelegateMethod;

    private String mOriginalPathAndQuery;

    /**
     * Creates an instance of this class.
     * 
     * @param matcherUri
     *            The matcher uri this delegate uri is associated with.
     * @param pathAndQuery
     *            The path and optional query string this delegate URI must handle.
     */
    public DelegateUri(MatcherUri matcherUri, String pathAndQuery) {

        this.mOriginalPathAndQuery = pathAndQuery;

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
        this.mQueryString = uri.getQuery();
    }

    /**
     * Gets the path portion of this URI. The path will already be normalized for a <a
     * href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a> (i.e. placeholder
     * names will be replaced by '*').
     * 
     * @return The normalized path for this URI.
     */
    public String getNormalizedPath() {
        return mPath;
    }

    /**
     * Checks if this URI has the specified placeholder name.
     * 
     * @param parameterName
     *            The name of the placeholder to check.
     * 
     * @return <tt>true</tt> if this URI has a path or query string placeholder named as parameterName, <tt>false</tt>
     *         otherwise.
     */
    public boolean containsPlaceholder(String parameterName) {

        return mPlaceholders.containsKey(parameterName);
    }

    /**
     * Gets a string representation of the position this named parameter appears in this URI's path and query string.
     * 
     * @param name
     *            The name of the path placeholder to check.
     * 
     * @return The position this named parameter appears in this URI's path. Will return a position for a path
     *         placeholder or a name for a query string placeholder. Returns <tt>null</tt> if the path does not contain
     *         the named placeholder.
     */
    public String getParameterPosition(String name) {

        UriPlaceholderParameter placeholder = mPlaceholders.get(name);

        return placeholder != null ? placeholder.getKey() : null;
    }

    /**
     * Gets the map o query string parameter names and their values for this URI.
     * 
     * @return The map o query string parameter names and values for this URI.
     */
    public Map<String, String> getQueryParameterNamesAndValues() {

        return mQueryStringParameterNamesAndValues;
    }

    /**
     * Gets the amount of query string parameters in this URI.
     * 
     * @return The number of query string parameters in this URI.
     */
    public int getQueryStringParameterCount() {

        return mQueryStringParameterNamesAndValues.size();
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

        return mPlaceholders.get(placeholderName).getUriPlaceholderType();
    }

    /**
     * Gets the delegate method for a query operation.
     * 
     * @return The DelegateMethod for the query operation or null if this URI does not respond to query requests.
     */
    public DelegateMethod getQueryDelegateMethod() {

        return mQueryDelegateMethod;
    }

    /**
     * Gets the delegate method for an update operation.
     * 
     * @return The DelegateMethod for the update operation or null if this URI does not respond to query requests.
     */
    public DelegateMethod getUpdateDelegateMethod() {

        return mUpdateDelegateMethod;
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

        mQueryDelegateMethod = delegateMethod;

        return delegateMethod;
    }

    /**
     * Creates a new DelegateMethod instance and registers it as an update delegate. Overrides any previously set update
     * DelegateMethod for this URI.
     * 
     * @param updateMethod
     *            The ExecutableElement for the method in the delegate class which will answer for queries against this
     *            URI.
     * 
     * @return The newly create and registered update DelegateMethod.
     */
    public DelegateMethod setUpdateDelegateMethod(ExecutableElement updateMethod) {

        DelegateMethod delegateMethod = new DelegateMethod(updateMethod, this);
        delegateMethod.setQueryParameterNames(this.getQueryParameterNamesAndValues().keySet());

        mUpdateDelegateMethod = delegateMethod;

        return delegateMethod;
    }

    /**
     * Parses the path and query string and finds placeholders.
     * 
     * @param pathAndQuery
     *            The path and query string to parse.
     */
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
                            queryVars[position], mOriginalPathAndQuery));
                }

                Matcher m = placeholderPattern.matcher(nameAndValue[1]);

                if (m.matches()) {

                    mQueryStringParameterNamesAndValues.put(nameAndValue[0], PLACEHOLDER_WILDCARD);
                    String placeholderName = m.group(1);
                    addQueryPlaceholder(placeholderName, nameAndValue[0]);
                } else {
                    mQueryStringParameterNamesAndValues.put(nameAndValue[0], nameAndValue[1]);
                }
            }
        }
    }

    private void addPathPlaceholder(String placeholderName, int position) {

        if (mPlaceholders.containsKey(placeholderName)) {

            throw new DuplicateUriPlaceholderException(placeholderName, mPlaceholders.get(placeholderName).getKey(),
                    Integer.toString(position));
        }

        UriPlaceholderParameter pathPlaceholder = new UriPlaceholderParameter(placeholderName, position);
        mPlaceholders.put(placeholderName, pathPlaceholder);
    }

    private void addQueryPlaceholder(String placeholderName, String queryParameterName) {

        if (mPlaceholders.containsKey(placeholderName)) {

            throw new DuplicateUriPlaceholderException(placeholderName, mPlaceholders.get(placeholderName).getKey(),
                    queryParameterName);
        }

        UriPlaceholderParameter queryPlaceholder = new UriPlaceholderParameter(placeholderName, queryParameterName);
        mPlaceholders.put(placeholderName, queryPlaceholder);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mAuthority == null) ? 0 : mAuthority.hashCode());
        result = prime * result + ((mPath == null) ? 0 : mPath.hashCode());
        result = prime * result + ((mQueryDelegateMethod == null) ? 0 : mQueryDelegateMethod.hashCode());
        result = prime * result
                + ((mQueryStringParameterNamesAndValues == null) ? 0 : mQueryStringParameterNamesAndValues.hashCode());
        result = prime * result + ((mUpdateDelegateMethod == null) ? 0 : mUpdateDelegateMethod.hashCode());
        return result;
    }

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
        if (mQueryDelegateMethod == null) {
            if (other.mQueryDelegateMethod != null)
                return false;
        } else if (!mQueryDelegateMethod.equals(other.mQueryDelegateMethod))
            return false;
        if (mQueryStringParameterNamesAndValues == null) {
            if (other.mQueryStringParameterNamesAndValues != null)
                return false;
        } else if (!mQueryStringParameterNamesAndValues.equals(other.mQueryStringParameterNamesAndValues))
            return false;
        if (mUpdateDelegateMethod == null) {
            if (other.mUpdateDelegateMethod != null)
                return false;
        } else if (!mUpdateDelegateMethod.equals(other.mUpdateDelegateMethod))
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
        return "DelegateUri [mAuthority=" + mAuthority + ", mPath=" + mPath + ", queryString=" + mQueryString + "]";
    }
}
