package com.nudroid.annotation.processor;

/**
 * Signal an internal error while processing the persistence annotations.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class AnnotationProcessorException extends RuntimeException {

    private static final long serialVersionUID = 379279303608925588L;

    /**
     * @see Exception#Exception()
     */
    public AnnotationProcessorException() {
        super();
    }

    /**
     * @see Exception#Exception(String)
     */
    public AnnotationProcessorException(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public AnnotationProcessorException(Throwable cause) {
        super(cause);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public AnnotationProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}