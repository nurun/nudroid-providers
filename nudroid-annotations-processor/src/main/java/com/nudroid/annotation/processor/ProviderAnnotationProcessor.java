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
 * <p>Annotation processor creating the bindings necessary for Android content provider delegates.</p> <p>
 * <h1>Logging</h1> <p> <p>This processor can be configured to display log messages during the annotation processing
 * rounds. Log messages are delivered through the processors {@link Messager} objects. In other words, they are issued
 * as compiler notes, warning or errors.</p> <p> <p> The logging level can be configured through the property
 * <tt>com.nudroid.annotation.processor.log.level</tt>.</p> <p> <p>The logging level can either be configured through a
 * processor property (with the -A option) or a system property (with a -D option). Processor property configuration
 * takes precedence over the system property.</p>
 */
@SupportedAnnotationTypes(
        {"com.nudroid.annotation.provider.delegate.ContentProvider", "com.nudroid.annotation.provider.delegate.Delete",
                "com.nudroid.annotation.provider.delegate.Insert", "com.nudroid.annotation.provider.delegate.Query",
                "com.nudroid.annotation.provider.delegate.Update",
                "com.nudroid.annotation.provider.delegate.intercept.InterceptorPointcut"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({"com.nudroid.annotation.processor.log.level"})
// TODO To avoid class name conflicts, use the annotated classes package and append .generated_ instead of creating
// all classes on same package
public class ProviderAnnotationProcessor extends AbstractProcessor {

    private static final String LOG_LEVEL_PROPERTY_NAME = "com.nudroid.annotation.processor.log.level";

    private LoggingUtils logger;

    private boolean initialized = false;

    private ContentProviderProcessor contentProviderProcessor;
    private QueryProcessor queryProcessor;
    private InterceptorPointcutProcessor interceptorPointcutProcessor;

    private SourceCodeWriter sourceCodeWriter;

    private int round = 0;

    private Metadata metadata;

    /**
     * {@inheritDoc}
     *
     * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {

        super.init(env);

        String logLevel = env.getOptions()
                .get(LOG_LEVEL_PROPERTY_NAME);

        if (logLevel == null) {
            logLevel = System.getProperty(LOG_LEVEL_PROPERTY_NAME);
        }

        logger = new LoggingUtils(env.getMessager(), logLevel);

        logger.debug("Initializing Nudroid persistence annotation processor.");

        Elements elementUtils = env.getElementUtils();
        Types typeUtils = env.getTypeUtils();

        final ProcessorContext processorContext =
                new ProcessorContext(processingEnv, new ProcessorUtils(typeUtils, elementUtils), logger);
        contentProviderProcessor = new ContentProviderProcessor(processorContext);
        queryProcessor = new QueryProcessor(processorContext);
        interceptorPointcutProcessor = new InterceptorPointcutProcessor(processorContext);
        sourceCodeWriter = new SourceCodeWriter(processorContext);
        metadata = new Metadata();
        initialized = true;

        logger.debug("Initialization complete.");
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        logger.info("Starting provider annotation processor round " + ++round);

        if (!initialized) {

            logger.error("Annotation processor not initialized. Aborting.");

            return false;
        }

        interceptorPointcutProcessor.process(roundEnv, metadata);
        contentProviderProcessor.process(roundEnv, metadata);
        queryProcessor.process(roundEnv, metadata);

        sourceCodeWriter.generateCompanionSourceCode(metadata);

        return true;
    }
}