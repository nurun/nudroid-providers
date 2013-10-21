package com.nudroid.annotation.processor.model;

/**
 * The type of the uri placeholder.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public enum UriPlaceholderType {

    /**
     * A placeholder appearing in the path segment of a URI.
     */
    PATH_PARAM,

    /**
     * A placeholder appearing in the query string segment of a URI.
     */
    QUERY_PARAM;
}
