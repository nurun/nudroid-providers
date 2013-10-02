package com.nudroid.persistence.annotation.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a <a
 * href="http://developer.android.com/reference/android/content/ContentProvider.html">ContentProvider</a> URI mapped to
 * a delegate method, along with information about it's placeholder names.
 * <p/>
 * Note: {@link Uri} objects do not take the query string portion into consideration when comparing itself to other
 * instances of this class (i.e. {@link Object#equals(Object)} and {@link Object#hashCode()}). If two {@link Uri}
 * objects are distinguised by the query string option alone, they are considered to be the same URI.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Uri {

    private static String PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";

    private int id;
    private String authority;
    private String path;
    private String queryString;
    private List<UriPlaceholderParameter> placeholders = new ArrayList<UriPlaceholderParameter>();
    private List<UriPlaceholderParameter> pathPlaceholders = new ArrayList<UriPlaceholderParameter>();
    private List<UriPlaceholderParameter> queryPlaceholders = new ArrayList<UriPlaceholderParameter>();
    private Set<String> queryParameterNames = new HashSet<String>();

    @SuppressWarnings("unused")
    private LoggingUtils logger;

    private String originalPath;

    /**
     * Creates an instance of this class.
     * 
     * @param authority
     *            The <a
     *            href="http://developer.android.com/reference/android/content/ContentProvider.html">ContentProvider</a>
     *            authority name.
     * @param path
     *            The mapped <a
     *            href="http://developer.android.com/reference/android/content/ContentProvider.html">ContentProvider</a>
     *            URI path.
     * @param logger
     *            An instance of the logger class.
     */
    Uri(String authority, String path, LoggingUtils logger) {

        this.originalPath = path;
        this.logger = logger;
        parsePlaceholders(path);

        String normalizedPath = path.replaceAll(PLACEHOLDER_REGEXP, "*");
        URI uri;

        try {
            uri = URI.create("content://" + authority
                    + (normalizedPath.startsWith("/") ? normalizedPath : "/" + normalizedPath));
        } catch (IllegalArgumentException e) {
            throw new IllegalUriPathException(e);
        }

        this.authority = authority;
        this.path = uri.getPath();
        this.queryString = uri.getQuery();
    }

    /**
     * Gets the authority name of this URI.
     * 
     * @return The authority name of this URI.
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Gets the path of this URI. The path will already be normalized for a <a
     * href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a> (i.e. placeholder
     * names will be replaced by '*').
     * 
     * @return The normalized path for this URI.
     */
    public String getNormalizedPath() {
        return path;
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
     * Checks if this URI has the provided named path placeholder.
     * 
     * @param parameterName
     *            The name of the placeholder to check.
     * 
     * @return <tt>true</tt> if this URI has a path placeholder named as parameterName, <tt>false</tt> otherwise.
     */
    boolean containsPathPlaceholder(String parameterName) {

        UriPlaceholderParameter pathParam = new UriPlaceholderParameter(parameterName, 0);

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
    boolean containsQueryPlaceholder(String parameterName) {

        UriPlaceholderParameter pathParam = new UriPlaceholderParameter(parameterName, 0);

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
    String getPathParameterPosition(String name) {

        UriPlaceholderParameter pathParam = new UriPlaceholderParameter(name, 0);

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
    String getQueryParameterPlaceholderName(String name) {

        UriPlaceholderParameter queryParam = new UriPlaceholderParameter(name, 0);
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

    private void parsePlaceholders(String path) {

        Pattern placeholderPattern = Pattern.compile(PLACEHOLDER_REGEXP);

        String[] pathAndQueryString = path.split("\\?");

        if (pathAndQueryString.length > 2) {

            throw new IllegalUriPathException(String.format("The path uri '%s' is invalid.", path));
        }

        if (pathAndQueryString.length >= 1) {

            String pathSection = pathAndQueryString[0];

            pathSection = pathSection.replaceAll("^/+", "");

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

                    throw new IllegalUriPathException(String.format("Path %s query string is invalid.", originalPath));
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

        UriPlaceholderParameter pathPlaceholder = new UriPlaceholderParameter(placeholderName, position);

        if (placeholders.contains(pathPlaceholder)) {

            throw new DuplicateUriPlaceholderException(placeholderName, placeholders.get(
                    placeholders.indexOf(pathPlaceholder)).getKey(), Integer.toString(position));
        }

        placeholders.add(pathPlaceholder);
        pathPlaceholders.add(pathPlaceholder);
    }

    private void addQueryPlaceholder(String placeholderName, String queryParameterName) {

        UriPlaceholderParameter queryPlaceholder = new UriPlaceholderParameter(placeholderName, queryParameterName);

        if (placeholders.contains(queryPlaceholder)) {

            throw new DuplicateUriPlaceholderException(placeholderName, placeholders.get(
                    placeholders.indexOf(queryPlaceholder)).getKey(), queryParameterName);
        }

        placeholders.add(queryPlaceholder);
        queryPlaceholders.add(queryPlaceholder);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((authority == null) ? 0 : authority.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Uri other = (Uri) obj;
        if (authority == null) {
            if (other.authority != null) return false;
        } else if (!authority.equals(other.authority)) return false;
        if (path == null) {
            if (other.path != null) return false;
        } else if (!path.equals(other.path)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Uri [id=" + id + ", authority=" + authority + ", path=" + path + ", queryString=" + queryString
                + ", pathPlaceholders=" + pathPlaceholders + ", queryPlaceholders=" + queryPlaceholders + "]";
    }
}
