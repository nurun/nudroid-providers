package com.nudroid.annotation.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

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
    private Types mTtypeUtils;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    QueryAnnotationProcessor(ProcessorContext processorContext) {

        this.mTtypeUtils = processorContext.typeUtils;
        this.mLogger = processorContext.logger;

        mContextType = processorContext.elementUtils.getTypeElement("android.content.Context").asType();
        mStringType = processorContext.elementUtils.getTypeElement(String.class.getName()).asType();
        mArrayOfStringsType = processorContext.typeUtils.getArrayType(mStringType);
        mUriType = processorContext.elementUtils.getTypeElement("android.net.Uri").asType();
    }

    /**
     * Process the {@link Query} annotations on this round.
     * 
     * @param roundEnv
     *            The round environment to process.
     * @param metadata
     *            The annotation metadata for the processor.
     */
    @SuppressWarnings("unchecked")
    void process(RoundEnvironment roundEnv, Metadata metadata) {

        Set<ExecutableElement> queryMethods = (Set<ExecutableElement>) roundEnv.getElementsAnnotatedWith(Query.class);

        mLogger.info("Start processing @Query annotations.");
        mLogger.trace("    Methods annotated with @Query for the round " + queryMethods);

        for (ExecutableElement queryMethod : queryMethods) {

            TypeElement enclosingClass = (TypeElement) queryMethod.getEnclosingElement();
            ContentProvider contentProviderDelegateAnnotation = enclosingClass.getAnnotation(ContentProvider.class);

            if (contentProviderDelegateAnnotation == null) {

                mLogger.error(
                        String.format("Enclosing class must be annotated with @%s", ContentProvider.class.getName()),
                        queryMethod);
                continue;
            }

            mLogger.trace("    Processing " + queryMethod);
            DelegateClass delegateClass = metadata.getDelegateClassForTypeElement(enclosingClass);
            processQueryOnMethod(enclosingClass, queryMethod, delegateClass, metadata);
            mLogger.trace("    Done processing " + queryMethod);
        }

        mLogger.info("Done processing @Query annotations.");
    }

    private void processQueryOnMethod(TypeElement enclosingClass, ExecutableElement queryMethod,
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
            return;
        } catch (DuplicateUriPlaceholderException e) {

            mLogger.trace(String.format("        Path '%s' has duplicated placeholder. Signaling compilation error.",
                    pathAndQuery));
            mLogger.error(
                    String.format("Placeholder '%s' appearing at position '%s' is already present at position '%s'",
                            e.getPlaceholderName(), e.getDuplicatePosition(), e.getExistingPosition()), queryMethod);
            return;
        }

        boolean hasValidAnnotations = hasValidAnnotations(enclosingClass, queryMethod, query, delegateUri);

        if (!hasValidAnnotations) {

            return;
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
            if (mTtypeUtils.isSameType(methodParameter.asType(), mStringType)) parameter.setString(true);

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

            if (!mTtypeUtils.isSameType(parameterType, requiredType)) {

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

                mLogger.trace(String
                        .format("        Multiple annotatoins on same parameter. Signaling compilatoin error."));
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
}
