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
import com.nudroid.annotation.processor.model.DelegateMethod;
import com.nudroid.annotation.processor.model.InterceptorPointAnnotationBlueprint;
import com.nudroid.annotation.processor.model.UriToMethodBinding;
import com.nudroid.annotation.processor.model.ValidationError;
import com.nudroid.annotation.provider.delegate.ContentProvider;
import com.nudroid.annotation.provider.delegate.Query;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Processes the Query annotations on a class.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class QueryProcessor {

    private static final String PATH_AND_QUERY_STRING_REGEXP = "[^\\?]*\\?.*";

    private final LoggingUtils logger;

    private final Types typeUtils;
    private final Elements elementUtils;

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The processor context parameter object.
     */
    QueryProcessor(ProcessorContext processorContext) {

        this.typeUtils = processorContext.typeUtils;
        this.elementUtils = processorContext.elementUtils;
        this.logger = processorContext.logger;
    }

    /**
     * Process the {@link Query} annotations on this round.
     *
     * @param roundEnv
     *         The round environment to process.
     * @param metadata
     *         the Metadata model to gather the results of the processing
     */
    //TODO Bug on nudroid annotations: If a parameter is added to the method signature but it is not present in the queryString (and also check path) it throws a NullPointerException.
    // TODO If using primitive type, (like long) app crashes when converting placeholders. See convert() on router for
    // details (it only Checks Long not long).
    void process(RoundEnvironment roundEnv, Metadata metadata) {

        logger.info("Start processing @Query annotations.");

        Set<? extends Element> queryMethods = roundEnv.getElementsAnnotatedWith(Query.class);

        if (queryMethods.size() > 0) {

            logger.trace(String.format("    Methods annotated with %s for the round:\n        - %s",
                    Query.class.getSimpleName(), queryMethods.stream()
                            .map(Element::toString)
                            .collect(Collectors.joining("\n        - "))));
        }

        for (Element queryMethod : queryMethods) {

            if (queryMethod instanceof ExecutableElement) {

                TypeElement enclosingClass = (TypeElement) queryMethod.getEnclosingElement();
                ContentProvider contentProviderDelegateAnnotation = enclosingClass.getAnnotation(ContentProvider.class);

                if (contentProviderDelegateAnnotation == null) {

                    logger.error(String.format("Enclosing class must be annotated with @%s",
                            ContentProvider.class.getName()), queryMethod);
                    continue;
                }

                logger.trace("    Processing " + queryMethod);
                DelegateClass delegateClass = metadata.getDelegateClassForTypeElement(enclosingClass);
                DelegateMethod delegateMethod = processQueryOnMethod((ExecutableElement) queryMethod, delegateClass);

                if (delegateMethod != null) {

                    logger.trace("    Checking for interceptors on method " + queryMethod);
                    processInterceptorsOnMethod(delegateMethod, metadata);
                }

                logger.trace("    Done processing " + queryMethod);
            }
        }

        logger.info("Done processing @Query annotations.");
    }

    private DelegateMethod processQueryOnMethod(ExecutableElement queryMethod, DelegateClass delegateClass) {

        Query query = queryMethod.getAnnotation(Query.class);
        String path = query.value();

        if (path.matches(PATH_AND_QUERY_STRING_REGEXP)) {

            logger.error("Query strings are not allowed in path expressions", queryMethod);
            return null;
        }

        DelegateMethod delegateMethod =
                new DelegateMethod.Builder(queryMethod).build(this.elementUtils, this.typeUtils, errors -> {
                    for (ValidationError error : errors) {
                        logger.error(error.getMessage(), error.getElement());
                    }
                });

        UriToMethodBinding uriToMethodBinding = new UriToMethodBinding.Builder().path(path)
                .delegateMethod(delegateMethod)
                .build(errors -> {

                    for (ValidationError error : errors) {
                        logger.error(error.getMessage(), error.getElement());
                    }
                });

        delegateClass.registerBindingForQuery(uriToMethodBinding, errors -> {
            for (ValidationError error : errors) {
                logger.error(error.getMessage(), error.getElement());
            }
        });

        return delegateMethod;
    }

    private void processInterceptorsOnMethod(DelegateMethod delegateMethod, Metadata metadata) {

        if (metadata.getInterceptorBlueprints()
                .size() == 0) {
            return;
        }

        List<? extends AnnotationMirror> annotationsMirrors = delegateMethod.getExecutableElement()
                .getAnnotationMirrors();

        for (AnnotationMirror mirror : annotationsMirrors) {

            for (InterceptorPointAnnotationBlueprint concreteAnnotation : metadata.getInterceptorBlueprints()) {

                logger.trace(
                        String.format("        Checking for interceptor %s.", concreteAnnotation.getTypeElement()));

                final TypeElement annotationTypeElement = concreteAnnotation.getTypeElement();

                // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same
                // qualified name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
                if (mirror.getAnnotationType()
                        .toString()
                        .equals(annotationTypeElement.asType()
                                .toString())) {

                    delegateMethod.addInterceptor(
                            concreteAnnotation.createInterceptorPoint(mirror, elementUtils, typeUtils, logger));
                    logger.trace(String.format("        Interceptor %s added to method.",
                            concreteAnnotation.getInterceptorTypeElement()));
                }
            }
        }
    }
}
