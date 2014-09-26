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

import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.provider.delegate.ContentProvider;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Processes the {@link ContentProvider} annotation on a {@link TypeElement}.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class ContentProviderProcessor {

    private final LoggingUtils logger;
    private final ProcessorUtils processorUtils;

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The context for the provider annotation processor.
     */
    ContentProviderProcessor(ProcessorContext processorContext) {

        this.logger = processorContext.logger;
        this.processorUtils = processorContext.processorUtils;
    }

    /**
     * Process the {@link ContentProvider} annotations on an annotation processor round.
     *
     * @param roundEnv
     *         the round environment to process
     * @param metadata
     *         the Metadata model to gather the results of the processing
     */
    void process(RoundEnvironment roundEnv, Metadata metadata) {

        logger.info(String.format("Start processing @%s annotations.", ContentProvider.class.getSimpleName()));

        Set<? extends Element> delegateClassTypes = roundEnv.getElementsAnnotatedWith(ContentProvider.class);

        if (delegateClassTypes.size() > 0) {

            String classesForTheRound = delegateClassTypes.stream()
                    .map(Element::toString)
                    .collect(Collectors.joining("\n        - "));

            logger.trace(String.format("    Classes annotated with @%s for the round:\n        - %s",
                    ContentProvider.class.getSimpleName(), classesForTheRound));
        }

        /*
         * Do not assume that because the @ContentProviderDelegate annotation can only be applied to types, only
         * TypeElements will be returned. Compilation errors on a class can let the compiler think the annotation is
         * applied to other elements even if it is correctly applied to a class, causing a class cast exception in
         * the forEach loop.
         */
        delegateClassTypes.stream()
                .filter(delegateClassType -> delegateClassType instanceof TypeElement)
                .forEach(delegateClassType -> {

                    logger.trace("    Processing " + delegateClassType);
                    processContentProviderDelegateAnnotation((TypeElement) delegateClassType, metadata);
                    logger.trace("    Done processing " + delegateClassType);
                });

        logger.info(String.format("Done processing @%s annotations.", ContentProvider.class.getSimpleName()));
    }

    private void processContentProviderDelegateAnnotation(TypeElement delegateClassType, Metadata metadata) {

        ContentProvider contentProviderDelegateAnnotation = delegateClassType.getAnnotation(ContentProvider.class);

        final DelegateClass delegateClass =
                new DelegateClass.Builder(contentProviderDelegateAnnotation, delegateClassType).build(processorUtils,
                        gatherer -> gatherer.logErrors(logger));

        metadata.registerNewDelegateClass(delegateClass, gatherer -> gatherer.logErrors(logger));
    }
}
