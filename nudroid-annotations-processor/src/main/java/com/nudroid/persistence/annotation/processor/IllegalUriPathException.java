package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class IllegalUriPathException extends RuntimeException {

    private static final long serialVersionUID = -2659207723560471364L;

    public IllegalUriPathException() {
        super();
    }

    public IllegalUriPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalUriPathException(String s) {
        super(s);
    }

    public IllegalUriPathException(Throwable cause) {
        super(cause);
    }
}
