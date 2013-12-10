package com.nudroid.provider.interceptor.cache;

/**
 * Returns the current time in milliseconds.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public interface Clock {

    /**
     * Gets the current time in milliseconds.
     * 
     * @return The current time, in milliseconds.
     */
    long currentTime();
}
