package com.nudroid.persistence.annotation.processor;

/**
 * Exception raised when the URI provided to the delegate method is not valid.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class IllegalUriPathException extends RuntimeException {

    private static final long serialVersionUID = -2659207723560471364L;

    /**
     * @see Exception#Exception()
     */
    public IllegalUriPathException() {
        super();
    }

    /**
     * @see Exception#Exception(String)
     */
    public IllegalUriPathException(String s) {
        super(s);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public IllegalUriPathException(Throwable cause) {
        super(cause);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public IllegalUriPathException(String message, Throwable cause) {
        super(message, cause);
    }
}
