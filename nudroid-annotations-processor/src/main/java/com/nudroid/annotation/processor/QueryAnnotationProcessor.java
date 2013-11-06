package com.nudroid.annotation.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.nudroid.annotation.processor.model.ConcreteAnnotation;
import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.DelegateMethod;
import com.nudroid.annotation.processor.model.DelegateUri;
import com.nudroid.annotation.processor.model.Parameter;
import com.nudroid.annotation.provider.delegate.ContentProvider;
import com.nudroid.annotation.provider.delegate.ContentUri;
import com.nudroid.annotation.provider.delegate.ContextRef;
import com.nudroid.annotation.provider.delegate.Projection;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.Selection;
import com.nudroid.annotation.provider.delegate.SelectionArgs;
import com.nudroid.annotation.provider.delegate.SortOrder;
import com.nudroid.annotation.provider.delegate.UriPlaceholder;

/**
 * Processes the Query annotations on a class.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class QueryAnnotationProcessor {

    private LoggingUtils mLogger;

    private TypeMirror mContextType;
    private TypeMirror mStringType;
    private TypeMirror mArrayOfStringsType;
    private TypeMirror mUriType;
    private Types mTypeUtils;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    QueryAnnotationProcessor(ProcessorContext processorContext) {

        this.mTypeUtils = processorContext.typeUtils;
        this.mLogger = processorContext.logger;

        mContextType = processorContext.elementUtils.getTypeElement("android.content.Context").asType();
        mStringType = processorContext.elementUtils.getTypeElement(String.class.getName()).asType();
        mArrayOfStringsType = processorContext.typeUtils.getArrayType(mStringType);
        mUriType = processorContext.elementUtils.getTypeElement("android.net.Uri").asType();
    }

    /**
     * Process the {@link Query} annotations on this round.
     * 
     * @param continuation
     *            The continuation environment.
     * @param roundEnv
     *            The round environment to process.
     * @param metadata
     *            The annotation metadata for the processor.
     */
    void process(Continuation continuation, RoundEnvironment roundEnv, Metadata metadata) {

        Set<? extends Element> queryMethods = continuation.getElementsAnotatedWith(Query.class, roundEnv);

        mLogger.info("Start processing @Query annotations.");
        mLogger.trace("    Methods annotated with @Query for the round " + queryMethods);

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
                DelegateMethod delegateMethod = processQueryOnMethod(enclosingClass, (ExecutableElement) queryMethod,
                        delegateClass, metadata);

                if (delegateMethod != null) {

                    mLogger.trace("    Checking for interceptors on method " + queryMethod);
                    processInterceptorsOnMethod(delegateMethod, metadata);
                }

                mLogger.trace("    Done processing " + queryMethod);
            }
        }

        mLogger.info("Done processing @Query annotations.");
    }

    private DelegateMethod processQueryOnMethod(TypeElement enclosingClass, ExecutableElement queryMethod,
            DelegateClass delegateClass, Metadata metadata) {

        Query query = queryMethod.getAnnotation(Query.class);
        String pathAndQuery = query.value();

        DelegateUri delegateUri = null;

        try {

            mLogger.trace(String.format("        Registering URI path '%s'.", pathAndQuery));
            delegateUri = delegateClass.registerPath(queryMethod, pathAndQuery);
            mLogger.trace(String.format("        Done registering URI path '%s'.", pathAndQuery));
        } catch (DuplicatePathException e) {

            mLogger.trace(String.format(
                    "        Path '%s' has already been registered by method %s. Signaling compilation error.",
                    pathAndQuery, e.getOriginalMoethod()));
            mLogger.error(
                    String.format("An equivalent path has already been registered by method '%s'",
                            e.getOriginalMoethod()), queryMethod);
            return null;
        } catch (DuplicateUriPlaceholderException e) {

            mLogger.trace(String.format("        Path '%s' has duplicated placeholder. Signaling compilation error.",
                    pathAndQuery));
            mLogger.error(
                    String.format("Placeholder '%s' appearing at position '%s' is already present at position '%s'",
                            e.getPlaceholderName(), e.getDuplicatePosition(), e.getExistingPosition()), queryMethod);
            return null;
        }

        boolean hasValidAnnotations = hasValidAnnotations(enclosingClass, queryMethod, query, delegateUri);

        if (!hasValidAnnotations) {

            return null;
        }

        DelegateMethod delegateMethod = new DelegateMethod(queryMethod, delegateUri);
        mLogger.trace(String.format("        Added delegate method %s to delegate class %s.", queryMethod,
                enclosingClass));

        delegateMethod.setQueryParameterNames(delegateUri.getQueryParameterNames());

        List<? extends VariableElement> parameters = queryMethod.getParameters();

        for (VariableElement methodParameter : parameters) {

            Parameter parameter = new Parameter();

            if (methodParameter.getAnnotation(ContextRef.class) != null) parameter.setContext(true);
            if (methodParameter.getAnnotation(Projection.class) != null) parameter.setProjection(true);
            if (methodParameter.getAnnotation(Selection.class) != null) parameter.setSelection(true);
            if (methodParameter.getAnnotation(SelectionArgs.class) != null) parameter.setSelectionArgs(true);
            if (methodParameter.getAnnotation(SortOrder.class) != null) parameter.setSortOrder(true);
            if (methodParameter.getAnnotation(ContentUri.class) != null) parameter.setContentUri(true);
            if (mTypeUtils.isSameType(methodParameter.asType(), mStringType)) parameter.setString(true);

            final UriPlaceholder uriPlaceholder = methodParameter.getAnnotation(UriPlaceholder.class);

            if (uriPlaceholder != null) {

                parameter.setPlaceholderName(uriPlaceholder.value());
                parameter.setUriPlaceholderType(delegateUri.getUriPlaceholderType(uriPlaceholder.value()));
                parameter.setKeyName(delegateUri.getParameterPosition(uriPlaceholder.value()));
            }

            delegateMethod.addParameter(parameter);
        }

        delegateClass.addMethod(delegateMethod);
        metadata.registerDelegateMethod(queryMethod, delegateMethod);

        return delegateMethod;
    }

    private boolean hasValidAnnotations(TypeElement enclosingClass, ExecutableElement method, Query query,
            DelegateUri uri) {

        boolean isValid = true;

        List<? extends VariableElement> parameters = method.getParameters();

        for (VariableElement parameterElement : parameters) {

            List<Class<?>> accumulatedAnnotations = new ArrayList<Class<?>>();

            final TypeMirror parameterType = parameterElement.asType();

            isValid = validateParameterAnnotation(parameterElement, ContextRef.class, parameterType, mContextType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(parameterElement, Projection.class, parameterType,
                    mArrayOfStringsType, accumulatedAnnotations);
            isValid = validateParameterAnnotation(parameterElement, Selection.class, parameterType, mStringType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(parameterElement, SelectionArgs.class, parameterType,
                    mArrayOfStringsType, accumulatedAnnotations);
            isValid = validateParameterAnnotation(parameterElement, SortOrder.class, parameterType, mStringType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(parameterElement, ContentUri.class, parameterType, mUriType,
                    accumulatedAnnotations);

            isValid = validateUriPlaceholderAnnotation(parameterElement, query, uri, accumulatedAnnotations);
        }

        return isValid;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean validateParameterAnnotation(VariableElement parameterElement, Class annotationClass,
            TypeMirror parameterType, TypeMirror requiredType, List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        if (parameterElement.getAnnotation(annotationClass) != null) {

            mLogger.trace(String.format("        Validating annotation @%s on prameter %s",
                    annotationClass.getSimpleName(), parameterElement));

            accumulatedAnnotations.add(annotationClass);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                mLogger.trace(String
                        .format("        Multiple annotatoins on same parameter. Signaling compilatoin error."));
                mLogger.error(
                        String.format("Parameters can only be annotated with one of %s.", accumulatedAnnotations),
                        parameterElement);
            }

            if (!mTypeUtils.isSameType(parameterType, requiredType)) {

                isValid = false;

                mLogger.trace(String.format("        Parameter is not of expected type. Signaling compilatoin error.",
                        annotationClass.getSimpleName(), parameterElement));
                mLogger.error(
                        String.format("Parameters annotated with @%s must be of type %s.",
                                annotationClass.getSimpleName(), requiredType), parameterElement);
            }
        }

        return isValid;
    }

    private boolean validateUriPlaceholderAnnotation(VariableElement parameterElement, Query query, DelegateUri uri,
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

                mLogger.trace(String.format("        Multiple annotatoins on same parameter."
                        + " Signaling compilatoin error."));
                mLogger.error(
                        String.format("Parameters can only be annotated with one of %s.", accumulatedAnnotations),
                        parameterElement);
            }

            if (!uri.containsPlaceholder(placeholderName)) {

                mLogger.trace(String.format(
                        "        Couldn't find placeholder %s on URI path. Signaling compilation error.",
                        placeholderName));
                mLogger.error(String.format("Could not find placeholder named '%s' on uri path '%s'.", placeholderName,
                        uriPathAndQuery), parameterElement);

                isValid = false;
            }
        }

        return isValid;
    }

    private void processInterceptorsOnMethod(DelegateMethod delegateMethod, Metadata metadata) {

        mLogger.info(String.format("Metadata instance: @%s", metadata));
        mLogger.trace(String.format("        Concrete annotations: %s.", metadata.getConcreteAnnotations()));

        for (ConcreteAnnotation concreteAnnotation : metadata.getConcreteAnnotations()) {

            mLogger.trace(String.format("        Checking for interceptor %s.", concreteAnnotation.getTypeElement()));

            List<? extends AnnotationMirror> annotationsMirrors = delegateMethod.getExecutableElement()
                    .getAnnotationMirrors();

            mLogger.trace(String.format("        Mirrors on method %s.", annotationsMirrors));

            for (AnnotationMirror mirror : annotationsMirrors) {

                final TypeElement annotationTypeElement = concreteAnnotation.getTypeElement();

                mLogger.trace(String.format("        Checking %s against %s.", mirror.getAnnotationType(),
                        annotationTypeElement.asType()));

                // Can't use Types.isSameType() due to error caused by modern IDEs incremental compilation. Method will
                // report they are not the same type even when thry are. Using the type qualified name instead.
                if (mirror.getAnnotationType().toString().equals(annotationTypeElement.asType().toString())) {

                    mLogger.trace(String.format("        Processing interceptor %s on method.", annotationTypeElement));

                    delegateMethod.addInterceptor(concreteAnnotation.getInterceptor());
                    mLogger.trace(String.format("        Interceptor %s added to method.",
                            concreteAnnotation.getInterceptorTypeElement()));
                }
            }
        }
    }
}
