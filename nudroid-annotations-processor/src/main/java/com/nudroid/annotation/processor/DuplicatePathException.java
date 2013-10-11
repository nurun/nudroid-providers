package com.nudroid.annotation.processor;

/**
 * Exception raised when the URI provided to the delegate method is not valid.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DuplicatePathException extends RuntimeException {

    private static final long serialVersionUID = -4364782083955709261L;

    /**
     * @see Exception#Exception()
     */
    public DuplicatePathException() {
        super();
    }

    /**
     * @see Exception#Exception(String)
     */
    public DuplicatePathException(String s) {
        super(s);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public DuplicatePathException(Throwable cause) {
        super(cause);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public DuplicatePathException(String message, Throwable cause) {
        super(message, cause);
    }
}
