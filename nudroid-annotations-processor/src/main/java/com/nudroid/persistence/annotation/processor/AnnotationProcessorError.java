package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class AnnotationProcessorError extends RuntimeException {

    private static final long serialVersionUID = 379279303608925588L;

    AnnotationProcessorError() {
        super();
    }

    AnnotationProcessorError(String s) {
        super(s);
    }

    AnnotationProcessorError(Throwable cause) {
        super(cause);
    }

    AnnotationProcessorError(String message, Throwable cause) {
        super(message, cause);
    }
}
