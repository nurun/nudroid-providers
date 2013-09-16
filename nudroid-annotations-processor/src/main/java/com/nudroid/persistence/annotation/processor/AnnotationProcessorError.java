package com.nudroid.persistence.annotation.processor;

/**
 * Signal an internal error while processing the persistence annotations.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class AnnotationProcessorError extends RuntimeException {

    private static final long serialVersionUID = 379279303608925588L;

    /**
     * @see Exception#Exception()
     */
    public AnnotationProcessorError() {
        super();
    }

    /**
     * @see Exception#Exception(String)
     */
    public AnnotationProcessorError(String s) {
        super(s);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public AnnotationProcessorError(Throwable cause) {
        super(cause);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public AnnotationProcessorError(String message, Throwable cause) {
        super(message, cause);
    }
}