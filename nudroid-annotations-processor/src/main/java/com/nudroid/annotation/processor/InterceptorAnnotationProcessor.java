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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.common.base.Joiner;
import com.nudroid.annotation.processor.model.AnnotationAttribute;
import com.nudroid.annotation.processor.model.InterceptorAnnotationBlueprint;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.provider.interceptor.ContentProviderInterceptor;
import com.nudroid.provider.interceptor.ProviderInterceptorPoint;

/**
 * Add validation to interceptor constructors. <br/> Processes @{@link ProviderInterceptorPoint} annotations.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class InterceptorAnnotationProcessor {

    private LoggingUtils mLogger;
    private Types mTypeUtils;
    private Elements mElementUtils;

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The processor context parameter object.
     */
    InterceptorAnnotationProcessor(ProcessorContext processorContext) {

        this.mLogger = processorContext.logger;
        this.mTypeUtils = processorContext.typeUtils;
        this.mElementUtils = processorContext.elementUtils;
    }

    /**
     * Process the {@link Query} annotations on this round.
     *
     * @param roundEnv
     *         The round environment to process.
     * @param metadata
     *         The annotation metadata for the processor.
     * @param continuation
     *         The continuation object for this processor.
     */
    void process(RoundEnvironment roundEnv, Metadata metadata, Continuation continuation) {

        mLogger.info(
                String.format("Start processing @%s annotations.", ProviderInterceptorPoint.class.getSimpleName()));

        Set<? extends Element> interceptorAnnotations =
                continuation.getElementsAnotatedWith(ProviderInterceptorPoint.class, roundEnv);

        if (interceptorAnnotations.size() > 0) {
            mLogger.trace(String.format("    Interfaces annotated with @%s for the round:\n        - %s",
                    ProviderInterceptorPoint.class.getSimpleName(), Joiner.on("\n        - ")
                    .skipNulls()
                    .join(interceptorAnnotations)));
        }

        for (Element interceptorAnnotation : interceptorAnnotations) {

            if (interceptorAnnotation instanceof TypeElement) {

                Element interceptorClass = interceptorAnnotation.getEnclosingElement();

                if (!ElementUtils.isClass(interceptorClass)) {

                    mLogger.error(String.format(
                            "Interceptor annotations must be static elements of an eclosing %s implementation",
                            ContentProviderInterceptor.class.getName()), interceptorAnnotation);
                    continue;
                }

                if (!mTypeUtils.isAssignable(interceptorClass.asType(),
                        mElementUtils.getTypeElement(ContentProviderInterceptor.class.getName())
                                .asType())) {

                    mLogger.error(String.format("Interceptor class %s must implement interface %s", interceptorClass,
                            ContentProviderInterceptor.class.getName()), interceptorAnnotation);
                    continue;
                }

                continuation.addTypeToContinuation((TypeElement) interceptorAnnotation);

                createConcreteAnnotationMetadata(interceptorAnnotation, metadata);

                mLogger.trace(
                        String.format("    Interceptor class for %s: %s", interceptorAnnotation, interceptorClass));
            }
        }

        mLogger.info(String.format("Done processing @%s annotations.", ProviderInterceptorPoint.class.getSimpleName()));
    }

    private InterceptorAnnotationBlueprint createConcreteAnnotationMetadata(Element interceptorAnnotation,
                                                                            Metadata metadata) {

        if (interceptorAnnotation instanceof TypeElement) {

            InterceptorAnnotationBlueprint annotation =
                    new InterceptorAnnotationBlueprint((TypeElement) interceptorAnnotation);

            final List<? extends Element> enclosedElements =
                    new ArrayList<Element>(interceptorAnnotation.getEnclosedElements());
            Collections.sort(enclosedElements, new Comparator<Element>() {

                @Override
                public int compare(Element o1, Element o2) {
                    return o1.getSimpleName()
                            .toString()
                            .compareTo(o2.getSimpleName()
                                    .toString());
                }
            });

            for (Element method : enclosedElements) {

                if (method instanceof ExecutableElement) {
                    annotation.addAttribute(new AnnotationAttribute((ExecutableElement) method));
                }
            }

            metadata.registerConcreteAnnotation(annotation);

            return annotation;
        }

        return null;
    }
}
