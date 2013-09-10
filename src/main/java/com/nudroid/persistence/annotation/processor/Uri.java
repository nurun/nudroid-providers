/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class Uri {

    private static String PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";

    private int id;
    private String authority;
    private String path;
    private String queryString;
    private List<String> parameters = new ArrayList<String>();
    private List<String> pathParams = new ArrayList<String>();
    private List<String> queryParams = new ArrayList<String>();

    public Uri(String path) {

        parsePlaceholders(path);

        String normalizedPath = path.replaceAll(PLACEHOLDER_REGEXP, "*");
        URI uri = URI.create("content://host"
                + (normalizedPath.startsWith("/") ? normalizedPath : "/" + normalizedPath));
        this.path = uri.getPath();
        this.queryString = uri.getQuery();
    }

    private void parsePlaceholders(String path) {
        Pattern pattern = Pattern.compile(PLACEHOLDER_REGEXP);

        int paramPosition = 0;

        String[] pathAndQueryString = path.split("\\?");

        if (pathAndQueryString.length > 2) {

            throw new IllegalUriPathException(String.format("The path uri '%s' is invalid.", path));
        }

        if (pathAndQueryString.length >= 1) {

            Matcher m = pattern.matcher(pathAndQueryString[0]);
            while (m.find()) {

                String paramName = m.group(1);
                addPathPlaceholder(paramName, paramPosition++);
            }
        }

        if (pathAndQueryString.length == 2) {

            Matcher m = pattern.matcher(pathAndQueryString[1]);
            while (m.find()) {

                String paramName = m.group(1);
                addQueryPlaceholder(paramName, paramPosition++);
            }
        }
    }

    public Uri(String authority, String path) {

        parsePlaceholders(path);

        String normalizedPath = path.replaceAll(PLACEHOLDER_REGEXP, "*");
        URI uri = URI.create("content://" + authority
                + (normalizedPath.startsWith("/") ? normalizedPath : "/" + normalizedPath));
        this.authority = authority;
        this.path = uri.getPath();
        this.queryString = uri.getQuery();
    }

    private void addPathPlaceholder(String paramName, int i) {

        if (parameters.contains(paramName)) {
            throw new DuplicateUriParameterException(paramName, parameters.indexOf(paramName), i);
        }

        parameters.add(paramName);
        pathParams.add(paramName);
    }

    private void addQueryPlaceholder(String paramName, int i) {

        if (parameters.contains(paramName)) {
            throw new DuplicateUriParameterException(paramName, parameters.indexOf(paramName), i);
        }

        parameters.add(paramName);
        queryParams.add(paramName);
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }

    public int getPlaceholderCount() {
        return parameters.size();
    }
    public int getPathPlaceholderCount() {
        return pathParams.size();
    }
    public int getQueryPlaceholderCount() {
        return queryParams.size();
    }

    public boolean containsPlaceholder(String parameterName) {

        return parameters.contains(parameterName);
    }

    public boolean containsPathPlaceholder(String parameterName) {

        return pathParams.contains(parameterName);
    }

    public boolean containsQueryPlaceholder(String parameterName) {

        return queryParams.contains(parameterName);
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

    public void setId(int uriId) {

        this.id = uriId;
    }

    public int getId() {

        return this.id;
    }

    public int getPathParameterPosition(String name) {

        return pathParams.indexOf(name);
    }

    @Override
    public String toString() {
        return "Uri [id=" + id + ", authority=" + authority + ", path=" + path + ", queryString=" + queryString
                + ", parameters=" + parameters + ", pathParams=" + pathParams + ", queryParams=" + queryParams + "]";
    }

    /**
     * @return
     */
    public List<String> getParametersList() {
        return parameters;
    }

    public String getAuthority() {
        return authority;
    }
}
