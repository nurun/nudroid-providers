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
import com.nudroid.annotation.processor.model.Parameter;
import com.nudroid.annotation.processor.model.UriMatcherPathPatternType;
import com.nudroid.annotation.provider.delegate.ContentProvider;
import com.nudroid.annotation.provider.delegate.ContentUri;
import com.nudroid.annotation.provider.delegate.ContentValuesRef;
import com.nudroid.annotation.provider.delegate.ContextRef;
import com.nudroid.annotation.provider.delegate.Projection;
import com.nudroid.annotation.provider.delegate.Selection;
import com.nudroid.annotation.provider.delegate.SelectionArgs;
import com.nudroid.annotation.provider.delegate.SortOrder;
import com.nudroid.annotation.provider.delegate.Update;
import com.nudroid.annotation.provider.delegate.UriPlaceholder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Processes the Update annotations on a class.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class UpdateProcessor {

    private static final String PRIMITIVE_INT_CLASS_NAME = "int";

    private final LoggingUtils mLogger;

    private final TypeMirror mContextType;
    private final TypeMirror mStringType;
    private final TypeMirror mArrayOfStringsType;
    private final TypeMirror mUriType;
    private final Types mTypeUtils;
    private final Elements mElementUtils;

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The processor context parameter object.
     */
    UpdateProcessor(ProcessorContext processorContext) {

        this.mTypeUtils = processorContext.typeUtils;
        this.mElementUtils = processorContext.elementUtils;
        this.mLogger = processorContext.logger;

        mContextType = processorContext.elementUtils.getTypeElement("android.content.Context")
                .asType();
        mStringType = processorContext.elementUtils.getTypeElement(String.class.getName())
                .asType();
        mArrayOfStringsType = processorContext.typeUtils.getArrayType(mStringType);
        mUriType = processorContext.elementUtils.getTypeElement("android.net.Uri")
                .asType();
    }

    /**
     * Process the {@link Update} annotations on this round.
     *
     * @param roundEnv
     *         The round environment to process.
     * @param metadata
     *         the Metadata model to gather the results of the processing
     */
    void process(RoundEnvironment roundEnv, Metadata metadata) {

        mLogger.info("Start processing @Update annotations.");

        Set<? extends Element> queryMethods = roundEnv.getElementsAnnotatedWith(Update.class);

        if (queryMethods.size() > 0) {

            mLogger.trace(String.format("    Methods annotated with %s for the round:\n        - %s",
                    Update.class.getSimpleName(), queryMethods.stream()
                            .map(Element::toString)
                            .collect(Collectors.joining("\n        - "))));
        }

        for (Element queryMethod : queryMethods) {

            if (queryMethod instanceof ExecutableElement) {

                TypeElement enclosingClass = (TypeElement) queryMethod.getEnclosingElement();
                ContentProvider contentProviderDelegateAnnotation = enclosingClass.getAnnotation(ContentProvider.class);

                if (contentProviderDelegateAnnotation == null) {

                    mLogger.error(String.format("Enclosing class must be annotated with @%s",
                            ContentProvider.class.getName()), queryMethod);
                    continue;
                }

                mLogger.trace("    Processing " + queryMethod);
                DelegateClass delegateClass = metadata.getDelegateClassForTypeElement(enclosingClass);
                DelegateMethod delegateMethod = processUpdateOnMethod((ExecutableElement) queryMethod, delegateClass);

                if (delegateMethod != null) {

                    mLogger.trace("    Checking for interceptors on method " + queryMethod);
                    processInterceptorsOnMethod(delegateMethod, metadata);
                }

                mLogger.trace("    Done processing " + queryMethod);
            }
        }

        mLogger.info("Done processing @Update annotations.");
    }

    private DelegateMethod processUpdateOnMethod(ExecutableElement methodElement, DelegateClass delegateClass) {

        Update query = methodElement.getAnnotation(Update.class);
        String pathAndQuery = query.value();

        UriToMethodBinding uriToMethodBinding;

        try {

            //TODO Check if there's a better way of doing this. Get the ParamTypePattern from the supported types map.
            List<UriMatcherPathPatternType> placeholderTypes = new ArrayList<>();

            List<? extends VariableElement> parameters = methodElement.getParameters();

            for (VariableElement param : parameters) {

                UriPlaceholder annotation = param.getAnnotation(UriPlaceholder.class);

                if (annotation != null) {

                    placeholderTypes.add(
                            UriMatcherPathPatternType.fromTypeMirror(param.asType(), mElementUtils, mTypeUtils));
                }
            }

            uriToMethodBinding = delegateClass.registerPathForUpdate(pathAndQuery, placeholderTypes);
            mLogger.trace(String.format("        Registering URI path '%s'.", pathAndQuery));
        } catch (DuplicatePathException e) {

            mLogger.trace(String.format(
                    "        Path '%s' has already been registered by method %s. Signaling compilation error.",
                    pathAndQuery, e.getOriginalMethod()));
            mLogger.error(String.format("An equivalent path has already been registered by method '%s'",
                    e.getOriginalMethod()), methodElement);
            return null;
        } catch (DuplicateUriPlaceholderException e) {

            mLogger.trace(String.format("        Path '%s' has duplicated placeholder. Signaling compilation error.",
                    pathAndQuery));
            mLogger.error(
                    String.format("Placeholder '%s' appearing at position '%s' is already present at position '%s'",
                            e.getPlaceholderName(), e.getDuplicatePosition(), e.getExistingPosition()), methodElement);
            return null;
        }

        boolean hasValidAnnotations = hasValidSignature(methodElement, query, uriToMethodBinding);

        if (!hasValidAnnotations) {

            return null;
        }

        DelegateMethod delegateMethod = uriToMethodBinding.setQueryDelegateMethod(methodElement);

        mLogger.trace(String.format("    Added delegate method %s.", methodElement));

        List<? extends VariableElement> parameters = methodElement.getParameters();

        for (VariableElement methodParameter : parameters) {

            Parameter parameter = new Parameter();

            if (methodParameter.getAnnotation(ContextRef.class) != null) parameter.setContext();
            if (methodParameter.getAnnotation(Projection.class) != null) parameter.setProjection();
            if (methodParameter.getAnnotation(Selection.class) != null) parameter.setSelection();
            if (methodParameter.getAnnotation(SelectionArgs.class) != null) parameter.setSelectionArgs();
            if (methodParameter.getAnnotation(SortOrder.class) != null) parameter.setSortOrder();
            if (methodParameter.getAnnotation(ContentUri.class) != null) parameter.setContentUri();
            if (methodParameter.getAnnotation(ContentValuesRef.class) != null) parameter.setContentValues();
            // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same qualified
            // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
            if (methodParameter.asType()
                    .toString()
                    .equals(mStringType.toString())) parameter.setString();

            final UriPlaceholder uriPlaceholder = methodParameter.getAnnotation(UriPlaceholder.class);

            if (uriPlaceholder != null) {

                parameter.setPlaceholderName(uriPlaceholder.value());
                parameter.setParameterType(methodParameter.asType()
                        .toString());
                parameter.setUriPlaceholderType(uriToMethodBinding.getUriPlaceholderType(uriPlaceholder.value()));
                parameter.setKeyName(uriToMethodBinding.getParameterPosition(uriPlaceholder.value()));
            }

            delegateMethod.addParameter(parameter);
        }

        // delegateClass.addMethod(delegateMethod);

        return delegateMethod;
    }

    private boolean hasValidSignature(ExecutableElement method, Update query, UriToMethodBinding uri) {

        boolean isValid = true;

        TypeMirror returnType = method.getReturnType();

        if (!PRIMITIVE_INT_CLASS_NAME.equals(returnType.toString())) {

            mLogger.trace(String.format("        Update %s method does not return expected int value.", method));
            mLogger.error(String.format("@Update annotated methods must return int."), method);

            isValid = false;
        }

        List<? extends VariableElement> parameters = method.getParameters();

        for (VariableElement parameterElement : parameters) {

            List<Class<?>> accumulatedAnnotations = new ArrayList<>();

            final TypeMirror parameterType = parameterElement.asType();

            isValid &= validateParameterAnnotation(parameterElement, ContextRef.class, parameterType, mContextType,
                    accumulatedAnnotations);
            isValid &=
                    validateParameterAnnotation(parameterElement, Projection.class, parameterType, mArrayOfStringsType,
                            accumulatedAnnotations);
            isValid &= validateParameterAnnotation(parameterElement, Selection.class, parameterType, mStringType,
                    accumulatedAnnotations);
            isValid &= validateParameterAnnotation(parameterElement, SelectionArgs.class, parameterType,
                    mArrayOfStringsType, accumulatedAnnotations);
            isValid &= validateParameterAnnotation(parameterElement, SortOrder.class, parameterType, mStringType,
                    accumulatedAnnotations);
            isValid &= validateParameterAnnotation(parameterElement, ContentUri.class, parameterType, mUriType,
                    accumulatedAnnotations);

            isValid &= validateUriPlaceholderAnnotation(parameterElement, query, uri, accumulatedAnnotations);
        }

        return isValid;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean validateParameterAnnotation(VariableElement parameterElement, Class annotationClass,
                                                TypeMirror parameterType, TypeMirror requiredType,
                                                List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        if (parameterElement.getAnnotation(annotationClass) != null) {

            accumulatedAnnotations.add(annotationClass);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                mLogger.trace(String.format(
                        "        Multiple incompatible annotatoins on same parameter [%s]. Signaling compilatoin error.",
                        accumulatedAnnotations));
                mLogger.error(String.format("Parameters can only be annotated with one of %s.", accumulatedAnnotations),
                        parameterElement);
            }

            // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same qualified
            // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
            if (!parameterType.toString()
                    .equals(requiredType.toString())) {

                isValid = false;

                mLogger.trace(
                        String.format("        Parameter is not of expected type %s. Signaling compilation error.",
                                requiredType));
                mLogger.error(String.format("Parameters annotated with @%s must be of type %s.",
                        annotationClass.getSimpleName(), requiredType), parameterElement);
            }
        }

        return isValid;
    }

    private boolean validateUriPlaceholderAnnotation(VariableElement parameterElement, Update query, UriToMethodBinding uri,
                                                     List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        final UriPlaceholder annotation = parameterElement.getAnnotation(UriPlaceholder.class);

        if (annotation != null) {

            mLogger.trace(String.format("        Validating annotation @%s on prameter %s",
                    UriPlaceholder.class.getSimpleName(), parameterElement));

            String placeholderName = annotation.value();
            final String uriPathAndQuery = query.value();

            accumulatedAnnotations.add(UriPlaceholder.class);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                mLogger.trace(String.format(
                        "        Multiple annotatoins on same parameter." + " Signaling compilatoin error."));
                mLogger.error(String.format("Parameters can only be annotated with one of %s.", accumulatedAnnotations),
                        parameterElement);
            }

            if (!uri.containsPlaceholder(placeholderName)) {

                mLogger.trace(
                        String.format("        Couldn't find placeholder %s on URI path. Signaling compilation error.",
                                placeholderName));
                mLogger.error(String.format("Could not find placeholder named '%s' on uri path '%s'.", placeholderName,
                        uriPathAndQuery), parameterElement);

                isValid = false;
            }
        }

        return isValid;
    }

    private void processInterceptorsOnMethod(DelegateMethod delegateMethod, Metadata metadata) {

        for (InterceptorPointAnnotationBlueprint concreteAnnotation : metadata.getInterceptorBlueprints()) {

            mLogger.trace(String.format("        Checking for interceptor %s.", concreteAnnotation.getTypeElement()));

            List<? extends AnnotationMirror> annotationsMirrors = delegateMethod.getExecutableElement()
                    .getAnnotationMirrors();

            for (AnnotationMirror mirror : annotationsMirrors) {

                final TypeElement annotationTypeElement = concreteAnnotation.getTypeElement();

                // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same
                // qualified name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
                if (mirror.getAnnotationType()
                        .toString()
                        .equals(annotationTypeElement.asType()
                                .toString())) {

                    delegateMethod.addInterceptor(
                            concreteAnnotation.createInterceptorPoint(mirror, mElementUtils, mTypeUtils, mLogger));
                    mLogger.trace(String.format("        Interceptor %s added to method.",
                            concreteAnnotation.getInterceptorTypeElement()));
                }
            }
        }
    }
}
