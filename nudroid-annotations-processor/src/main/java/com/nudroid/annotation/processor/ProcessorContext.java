package com.nudroid.annotation.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Parameter object holding the context variables for a processor run.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class ProcessorContext {

    /**
     * The processing environment object being used by this processor.
     */
    final ProcessingEnvironment processingEnv;

    /**
     * The elements utility object being used by this processor.
     */
    final Elements elementUtils;

    /**
     * The types utility object being used by this processor.
     */
    final Types typeUtils;

    /**
     * The logger object being used by this processor.
     */
    final LoggingUtils logger;

    /**
     * The continuation object for this compilation.
     */
    final Continuation continuation;

    /**
     * Creates an instance of this parameter object.
     * 
     * @param processingEnv
     *            The processing environment instance.
     * @param elementUtils
     *            The elements utility instance.
     * @param typeUtils
     *            The type utilities instance.
     * @param logger
     *            The logger instance.
     * @param continuation
     */
    public ProcessorContext(ProcessingEnvironment processingEnv, Elements elementUtils, Types typeUtils,
            LoggingUtils logger, Continuation continuation) {

        this.processingEnv = processingEnv;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.logger = logger;
        this.continuation = continuation;
    }
}