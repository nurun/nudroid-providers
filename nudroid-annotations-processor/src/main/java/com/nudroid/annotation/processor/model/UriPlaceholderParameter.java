package com.nudroid.annotation.processor.model;

/**
 * A placeholder in a <a
 * href="http://developer.android.com/reference/android/content/ContentProvider.html">ContentProvider</a> URI.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class UriPlaceholderParameter {

    private final String mKey;
    private final String mName;
    private final UriPlaceholderType mUriPlaceholderType;

    /**
     * Creates an instance of this class for a path placeholder.
     * 
     * @param name
     *            The name of the placeholder.
     * @param position
     *            The position it appears on the URI path.
     */
    UriPlaceholderParameter(String name, int position) {

        super();
        this.mName = name;
        this.mKey = Integer.toString(position);
        this.mUriPlaceholderType = UriPlaceholderType.PATH_PARAM;
    }

    /**
     * Creates an instance of this class for a query string placeholder.
     * 
     * @param name
     *            The name of the placeholder.
     * @param queryParameterName
     *            The name of the query string parameter it appears on the URI query string.
     */
    UriPlaceholderParameter(String name, String queryParameterName) {

        super();
        this.mKey = queryParameterName;
        this.mName = name;
        this.mUriPlaceholderType = UriPlaceholderType.QUERY_PARAM;
    }

    /**
     * Get's the string representation of the key identifying this parameter's position in the URI (i.e. the position
     * for a path placeholder or the query string name for a query string placeholder).
     * 
     * @return The string representation of the key identifying this parameter's position in the URI
     */
    String getKey() {

        return mKey;
    }

    /**
     * Gets the type of this placeholder.
     * 
     * @return The type of this placeholder.
     */
    UriPlaceholderType getUriPlaceholderType() {
        return mUriPlaceholderType;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        return result;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UriPlaceholderParameter other = (UriPlaceholderParameter) obj;
        if (mName == null) {
            if (other.mName != null) return false;
        } else if (!mName.equals(other.mName)) return false;
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
        return "UriPlaceholderParameter [key=" + mKey + ", name=" + mName + "]";
    }
}
