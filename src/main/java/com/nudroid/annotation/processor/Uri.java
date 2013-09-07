/**
 * 
 */
package com.nudroid.annotation.processor;

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

    private String path;
    private String queryString;
    private List<String> parameters = new ArrayList<String>();

    public Uri(String path) {

        Pattern pattern = Pattern.compile(PLACEHOLDER_REGEXP);
        Matcher m = pattern.matcher(path);

        int paramPosition = 0;

        while (m.find()) {

            String paramName = m.group(1);
            addParameter(paramName, paramPosition++);
        }

        String normalizedPath = path.replaceAll(PLACEHOLDER_REGEXP, "*");
        URI uri = URI.create("content://host"
                + (normalizedPath.startsWith("/") ? normalizedPath : "/" + normalizedPath));
        this.path = uri.getPath();
        this.queryString = uri.getQuery();
    }

    private void addParameter(String paramName, int i) {

        if (parameters.contains(paramName)) {
            throw new DuplicateUriParameterException(paramName, parameters.indexOf(paramName), i);
        }

        parameters.add(paramName);
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }

    @Override
    public String toString() {
        return "Uri [path=" + path + ", queryString=" + queryString + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((queryString == null) ? 0 : queryString.hashCode());
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
        Uri other = (Uri) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (queryString == null) {
            if (other.queryString != null)
                return false;
        } else if (!queryString.equals(other.queryString))
            return false;
        return true;
    }
}
