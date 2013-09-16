package com.nudroid.persistence.annotation.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Uri {

    private static String PATH_PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";
    private static String QUERY_PLACEHOLDER_REGEXP = "([^\\=]+)\\=\\{([^\\}]+)\\}";

    private int id;
    private String authority;
    private String path;
    private String queryString;
    private List<UriPlaceholderParameter> placeholders = new ArrayList<UriPlaceholderParameter>();
    private List<UriPlaceholderParameter> pathPlaceholders = new ArrayList<UriPlaceholderParameter>();
    private List<UriPlaceholderParameter> queryPlaceholders = new ArrayList<UriPlaceholderParameter>();

    @SuppressWarnings("unused")
    private LoggingUtils logger;

    Uri(String authority, String path, LoggingUtils logger) {

        this.logger = logger;
        parsePlaceholders(path);

        String normalizedPath = path.replaceAll(PATH_PLACEHOLDER_REGEXP, "*");
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

    public int getId() {

        return this.id;
    }

    void setId(int uriId) {

        this.id = uriId;
    }

    boolean containsPathPlaceholder(String parameterName) {

        UriPlaceholderParameter pathParam = new UriPlaceholderParameter(parameterName, 0);

        return pathPlaceholders.contains(pathParam);
    }

    boolean containsQueryPlaceholder(String parameterName) {

        UriPlaceholderParameter pathParam = new UriPlaceholderParameter(parameterName, 0);

        return queryPlaceholders.contains(pathParam);
    }

    String getPathParameterPosition(String name) {

        UriPlaceholderParameter pathParam = new UriPlaceholderParameter(name, 0);

        return pathPlaceholders.get(pathPlaceholders.indexOf(pathParam)).getKey();
    }

    String getQueryParameterPlaceholderName(String name) {

        UriPlaceholderParameter queryParam = new UriPlaceholderParameter(name, 0);
        return queryPlaceholders.get(queryPlaceholders.indexOf(queryParam)).getKey();
    }

    private void parsePlaceholders(String path) {

        Pattern pathPattern = Pattern.compile(PATH_PLACEHOLDER_REGEXP);
        Pattern queryPattern = Pattern.compile(QUERY_PLACEHOLDER_REGEXP);

        String[] pathAndQueryString = path.split("\\?");

        if (pathAndQueryString.length > 2) {

            throw new IllegalUriPathException(String.format("The path uri '%s' is invalid.", path));
        }

        if (pathAndQueryString.length >= 1) {

            String pathSection = pathAndQueryString[0];

            pathSection = pathSection.replaceAll("^/+", "");

            String[] pathElements = pathSection.split("/");

            for (int position = 0; position < pathElements.length; position++) {

                Matcher m = pathPattern.matcher(pathElements[position]);

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

                Matcher m = queryPattern.matcher(queryVars[position]);

                if (m.find()) {

                    String queryParameterName = m.group(1);
                    String placeholderName = m.group(2);
                    addQueryPlaceholder(placeholderName, queryParameterName);
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
