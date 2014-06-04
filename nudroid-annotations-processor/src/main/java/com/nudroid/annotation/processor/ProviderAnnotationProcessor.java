/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.nudroid.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Annotation processor creating the bindings necessary for Android content provider delegates.
 * 
 * <h1>Logging</h1> This processor can be configured to display log messages during the annotation processing rounds.
 * Log messages are delivered through the processors {@link Messager} objects. In other words, they are issued as
 * compiler notes, warning or errors.
 * <p/>
 * The logging level can be configured through the property com.nudroid.annotation.processor.log.level. The logging
 * level can either be configured through a processor property (with the -A option) or a system property (with a -D
 * option). Processor property configuration takes precedence over the system property.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
// @f[off]
@SupportedAnnotationTypes({
    "com.nudroid.annotation.provider.delegate.ContentProvider",
    "com.nudroid.annotation.provider.delegate.Delete",
    "com.nudroid.annotation.provider.delegate.Insert",
    "com.nudroid.annotation.provider.delegate.Query",
    "com.nudroid.annotation.provider.delegate.Update",

    "com.nudroid.provider.interceptor.ProviderInterceptorPoint" })
// @f[on]
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({ "com.nudroid.annotation.processor.log.level", "com.nudroid.annotation.processor.continuation.file" })
public class ProviderAnnotationProcessor extends AbstractProcessor {

    private static final String CONTINUATION_FILE_PROPERTY_NAME = "com.nudroid.annotation.processor.continuation.file";

    private static final String LOG_LEVEL_PROPERTY_NAME = "com.nudroid.annotation.processor.log.level";

    private LoggingUtils mLogger;

    private Elements elementUtils;
    private Types typeUtils;
    private boolean initialized = false;

    private ContentProviderDelegateAnnotationProcessor contentProviderDelegateAnnotationProcessor;
    private QueryAnnotationProcessor queryAnnotationProcessor;
    private InterceptorAnnotationProcessor interceptorAnnotationProcessor;

    private SourceCodeGenerator sourceCodeGenerator;

    private int mRound = 0;

    private Continuation mContinuation;

    private Metadata mMetadata;

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {

        super.init(env);

        String logLevel = env.getOptions().get(LOG_LEVEL_PROPERTY_NAME);

        if (logLevel == null) {
            logLevel = System.getProperty(LOG_LEVEL_PROPERTY_NAME);
        }

        mLogger = new LoggingUtils(env.getMessager(), logLevel);

        String continuationFile = env.getOptions().get(CONTINUATION_FILE_PROPERTY_NAME);

        if (continuationFile == null) {
            continuationFile = System.getProperty(CONTINUATION_FILE_PROPERTY_NAME);
        }

        mLogger.debug("Initializing nudroid persistence annotation processor.");

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();

        try {

            final ProcessorContext processorContext = new ProcessorContext(processingEnv, elementUtils, typeUtils,
                    mLogger);
            mContinuation = new Continuation(processorContext, continuationFile);
            mContinuation.loadContinuation();
            contentProviderDelegateAnnotationProcessor = new ContentProviderDelegateAnnotationProcessor(
                    processorContext);
            queryAnnotationProcessor = new QueryAnnotationProcessor(processorContext);
            interceptorAnnotationProcessor = new InterceptorAnnotationProcessor(processorContext);
            sourceCodeGenerator = new SourceCodeGenerator(processorContext);
            mMetadata = new Metadata();
            initialized = true;

            mLogger.debug("Initialization complete.");
        } catch (Exception e) {

            mLogger.error("Unable to load continuation index file. Aborting annotation processor: " + e.getMessage());
            throw new AnnotationProcessorException(e);
        }
    }

    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
     *      javax.annotation.processing.RoundEnvironment)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        mLogger.info("Starting provider annotation processor round " + ++mRound);

        if (!initialized) {

            mLogger.error("Annotation processor not initialized. Aborting.");

            return false;
        }

        interceptorAnnotationProcessor.process(roundEnv, mMetadata, mContinuation);
        contentProviderDelegateAnnotationProcessor.process(mContinuation, roundEnv, mMetadata);
        queryAnnotationProcessor.process(mContinuation, roundEnv, mMetadata);

        sourceCodeGenerator.generateCompanionSourceCode(mMetadata);

        // Continuation elements are to be processed on the first round only.
        mContinuation.flushStack();

        if (roundEnv.processingOver()) {

            mContinuation.saveContinuation();
        }

        return true;
    }
}