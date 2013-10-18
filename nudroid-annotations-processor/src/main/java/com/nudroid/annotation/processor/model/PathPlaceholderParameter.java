package com.nudroid.annotation.processor.model;

/**
 * A placeholder in a <a
 * href="http://developer.android.com/reference/android/content/ContentProvider.html">ContentProvider</a> URI.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class PathPlaceholderParameter {

    private String key;
    private String name;

    /**
     * Creates an instance of this class for a path placeholder.
     * 
     * @param name
     *            The name of the placeholder.
     * @param position
     *            The position it appears on the URI path.
     */
    PathPlaceholderParameter(String name, int position) {

        super();
        this.name = name;
        this.key = Integer.toString(position);
    }

    /**
     * Creates an instance of this class for a query string placeholder.
     * 
     * @param name
     *            The name of the placeholder.
     * @param queryParameterName
     *            The name of the query string parameter it appears on the URI query string.
     */
    PathPlaceholderParameter(String name, String queryParameterName) {

        super();
        this.key = queryParameterName;
        this.name = name;
    }

    /**
     * Get's the string representation of the key identifying this parameter's position in the URI (i.e. the position
     * for a path placeholder or the query string name for a query string placeholder).
     * 
     * @return The string representation of the key identifying this parameter's position in the URI
     */
    String getKey() {

        return key;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PathPlaceholderParameter other = (PathPlaceholderParameter) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UriPlaceholderParameter [key=" + key + ", name=" + name + "]";
    }
}