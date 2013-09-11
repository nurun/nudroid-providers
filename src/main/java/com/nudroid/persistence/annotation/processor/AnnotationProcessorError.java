package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class AnnotationProcessorError extends RuntimeException {

    private static final long serialVersionUID = 379279303608925588L;

    public AnnotationProcessorError() {
        super();
    }

    public AnnotationProcessorError(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationProcessorError(String s) {
        super(s);
    }

    public AnnotationProcessorError(Throwable cause) {
        super(cause);
    }
}
