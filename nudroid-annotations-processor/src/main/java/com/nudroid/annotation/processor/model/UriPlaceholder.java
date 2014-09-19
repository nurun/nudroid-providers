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

/**
 * A placeholder in a delegate method annotation URI.
 * <p>
 * Can represent both a placeholder appearing in the path segment or in the query string. If appearing in a path
 * segment, this placeholder key is it's position in the path segments (beginning with 0). If appearing in a query
 * string, this placeholder key is the name of the query string parameter.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class UriPlaceholder {

    private final String mKey;
    private final String mName;
    private final UriPlaceholderType mUriPlaceholderType;

    /**
     * Creates an instance of this class for a path placeholder.
     *
     * @param name
     *         The name of the placeholder.
     * @param position
     *         The position it appears on the URI path.
     */
    UriPlaceholder(String name, int position) {

        super();
        this.mName = name;
        this.mKey = Integer.toString(position);
        this.mUriPlaceholderType = UriPlaceholderType.PATH_PARAM;
    }

    /**
     * Creates an instance of this class for a query string placeholder.
     *
     * @param name
     *         The name of the placeholder.
     * @param queryParameterName
     *         The name of the query string parameter it appears on the URI query string.
     */
    UriPlaceholder(String name, String queryParameterName) {

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
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UriPlaceholder other = (UriPlaceholder) obj;
        if (mName == null) {
            if (other.mName != null) return false;
        } else if (!mName.equals(other.mName)) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UriPlaceholderParameter [key=" + mKey + ", name=" + mName + "]";
    }
}
