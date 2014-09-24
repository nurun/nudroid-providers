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

import com.nudroid.annotation.processor.model.AnnotationElement;
import com.nudroid.annotation.processor.model.InterceptorPointAnnotationBlueprint;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.intercept.InterceptorPointcut;
import com.nudroid.provider.delegate.ContentProviderDelegate;
import com.nudroid.provider.interceptor.ContentProviderInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Add validation to interceptor constructors. <br/> Processes @{@link com.nudroid.annotation.provider.delegate.intercept.InterceptorPointcut}
 * annotations.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class InterceptorPointcutProcessor {

    private final ProcessorUtils processorUtils;
    private final LoggingUtils logger;

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The processor context parameter object.
     */
    InterceptorPointcutProcessor(ProcessorContext processorContext) {

        this.processorUtils = processorContext.processorUtils;
        this.logger = processorContext.logger;
    }

    /**
     * Process the {@link Query} annotations on this round.
     *
     * @param roundEnv
     *         The round environment to process.
     * @param metadata
     *         The annotation metadata for the processor.
     */
    void process(RoundEnvironment roundEnv, Metadata metadata) {

        logger.info(String.format("Start processing @%s annotations.", InterceptorPointcut.class.getSimpleName()));

        Set<? extends Element> interceptorAnnotations = roundEnv.getElementsAnnotatedWith(InterceptorPointcut
                .class);

        if (interceptorAnnotations.size() > 0) {

            logger.trace(String.format("    Interfaces annotated with @%s for the round:\n        - %s",
                    InterceptorPointcut.class.getSimpleName(), interceptorAnnotations.stream()
                            .map(Element::toString)
                            .collect(Collectors.joining("\n        - "))));
        }

        for (Element interceptorAnnotation : interceptorAnnotations) {

            /* This check is required. Compilation error might make the compiler think the annotation has been
            applied on anther element, even if the target of the annotation is ANNOTATION_TYPE */
            if (interceptorAnnotation instanceof TypeElement) {

                Element interceptorClass = interceptorAnnotation.getEnclosingElement();

                /* Interceptor annotations must be inner classes of the actual interceptor implementation. */
                if (!processorUtils.isClass(interceptorClass)) {

                    logger.error(String.format(
                            "Interceptor annotations must be static elements of an enclosing %s implementation",
                            ContentProviderInterceptor.class.getName()), interceptorAnnotation);
                    continue;
                }

                if (!processorUtils.implementsInterface((TypeElement) interceptorClass,
                        ContentProviderInterceptor.class)) {

                    logger.error(String.format("Interceptor class %s must implement interface %s", interceptorClass,
                                    com.nudroid.provider.interceptor.ContentProviderInterceptor.class.getName()),
                            interceptorAnnotation);
                    continue;
                }

                createConcreteAnnotationMetadata(interceptorAnnotation, metadata);

                logger.trace(
                        String.format("    Interceptor class for %s: %s", interceptorAnnotation, interceptorClass));
            }
        }

        logger.info(String.format("Done processing @%s annotations.", InterceptorPointcut.class.getSimpleName()));
    }

    private void createConcreteAnnotationMetadata(Element interceptorAnnotation, Metadata metadata) {

        InterceptorPointAnnotationBlueprint annotationBlueprint =
                new InterceptorPointAnnotationBlueprint((TypeElement) interceptorAnnotation);

        final List<? extends Element> annotationProperties = getSortedAnnotationProperties(interceptorAnnotation);

        annotationProperties.stream()
                .filter(method -> method instanceof ExecutableElement)
                .forEach(method -> annotationBlueprint.addAttribute(
                        new AnnotationElement.Builder((ExecutableElement) method).build(processorUtils,
                                gatherer -> gatherer.logErrors(logger))));

        metadata.registerAnnotationBlueprint(annotationBlueprint);
    }

    private List<? extends Element> getSortedAnnotationProperties(Element interceptorAnnotation) {

        final List<? extends Element> enclosedElements = new ArrayList<>(interceptorAnnotation.getEnclosedElements());
        Collections.sort(enclosedElements, (o1, o2) -> o1.getSimpleName()
                .toString()
                .compareTo(o2.getSimpleName()
                        .toString()));

        return enclosedElements;
    }
}
