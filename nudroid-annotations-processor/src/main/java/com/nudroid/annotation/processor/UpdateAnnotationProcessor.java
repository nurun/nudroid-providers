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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.common.base.Joiner;
import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.DelegateMethod;
import com.nudroid.annotation.processor.model.DelegateUri;
import com.nudroid.annotation.processor.model.InterceptorBlueprint;
import com.nudroid.annotation.processor.model.Parameter;
import com.nudroid.annotation.provider.delegate.ContentProvider;
import com.nudroid.annotation.provider.delegate.ContentUri;
import com.nudroid.annotation.provider.delegate.ContextRef;
import com.nudroid.annotation.provider.delegate.Projection;
import com.nudroid.annotation.provider.delegate.Selection;
import com.nudroid.annotation.provider.delegate.SelectionArgs;
import com.nudroid.annotation.provider.delegate.SortOrder;
import com.nudroid.annotation.provider.delegate.Update;
import com.nudroid.annotation.provider.delegate.UriPlaceholder;

/**
 * Processes the Update annotations on a class.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class UpdateAnnotationProcessor {

    private static final String ANDROID_DATABASE_CURSOR_CLASS_NAME = "android.database.Cursor";

    private LoggingUtils mLogger;

    private TypeMirror mContextType;
    private TypeMirror mStringType;
    private TypeMirror mArrayOfStringsType;
    private TypeMirror mUriType;
    private Types mTypeUtils;
    private Elements mElementUtils;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    UpdateAnnotationProcessor(ProcessorContext processorContext) {

        this.mTypeUtils = processorContext.typeUtils;
        this.mElementUtils = processorContext.elementUtils;
        this.mLogger = processorContext.logger;

        mContextType = processorContext.elementUtils.getTypeElement("android.content.Context").asType();
        mStringType = processorContext.elementUtils.getTypeElement(String.class.getName()).asType();
        mArrayOfStringsType = processorContext.typeUtils.getArrayType(mStringType);
        mUriType = processorContext.elementUtils.getTypeElement("android.net.Uri").asType();
    }

    /**
     * Process the {@link Update} annotations on this round.
     * 
     * @param continuation
     *            The continuation environment.
     * @param roundEnv
     *            The round environment to process.
     * @param metadata
     *            The annotation metadata for the processor.
     */
    void process(Continuation continuation, RoundEnvironment roundEnv, Metadata metadata) {

        mLogger.info("Start processing @Update annotations.");

        Set<? extends Element> updateMethods = continuation.getElementsAnotatedWith(Update.class, roundEnv);

        if (updateMethods.size() > 0) {
            
            mLogger.trace(String.format("    Methods annotated with %s for the round:\n        - %s",
                    Update.class.getSimpleName(), Joiner.on("\n        - ").skipNulls().join(updateMethods)));
        }

        for (Element updateMethod : updateMethods) {

            if (updateMethod instanceof ExecutableElement) {

                TypeElement enclosingClass = (TypeElement) updateMethod.getEnclosingElement();
                ContentProvider contentProviderDelegateAnnotation = enclosingClass.getAnnotation(ContentProvider.class);

                if (contentProviderDelegateAnnotation == null) {

                    mLogger.error(String.format("Enclosing class must be annotated with @%s",
                            ContentProvider.class.getName()), updateMethod);
                    continue;
                }

                mLogger.trace("    Processing " + updateMethod);
                DelegateClass delegateClass = metadata.getDelegateClassForTypeElement(enclosingClass);
                DelegateMethod delegateMethod = processUpdateOnMethod((ExecutableElement) updateMethod, delegateClass,
                        metadata);

                if (delegateMethod != null) {

                    mLogger.trace("    Checking for interceptors on method " + updateMethod);
                    processInterceptorsOnMethod(delegateMethod, metadata);
                }

                mLogger.trace("    Done processing " + updateMethod);
            }
        }

        mLogger.info("Done processing @Update annotations.");
    }

    private DelegateMethod processUpdateOnMethod(ExecutableElement queryMethod, DelegateClass delegateClass,
            Metadata metadata) {

        Update update = queryMethod.getAnnotation(Update.class);
        String pathAndQuery = update.value();

        DelegateUri delegateUri = null;

        try {

            delegateUri = delegateClass.registerPathForUpdate(queryMethod, pathAndQuery);
            mLogger.trace(String.format("        Registering URI path '%s'.", pathAndQuery));
        } catch (DuplicatePathException e) {

            mLogger.trace(String.format(
                    "        Path '%s' has already been registered by method %s. Signaling compilation error.",
                    pathAndQuery, e.getOriginalMethod()));
            mLogger.error(
                    String.format("An equivalent path has already been registered by method '%s'",
                            e.getOriginalMethod()), queryMethod);
            return null;
        } catch (DuplicateUriPlaceholderException e) {

            mLogger.trace(String.format("        Path '%s' has duplicated placeholder. Signaling compilation error.",
                    pathAndQuery));
            mLogger.error(
                    String.format("Placeholder '%s' appearing at position '%s' is already present at position '%s'",
                            e.getPlaceholderName(), e.getDuplicatePosition(), e.getExistingPosition()), queryMethod);
            return null;
        }

        boolean hasValidAnnotations = hasValidSignature(queryMethod, update, delegateUri);

        if (!hasValidAnnotations) {

            return null;
        }

        DelegateMethod delegateMethod = new DelegateMethod(queryMethod, delegateUri);
        mLogger.trace(String.format("    Added delegate method %s.", queryMethod));

        delegateMethod.setQueryParameterNames(delegateUri.getQueryParameterNames());

        List<? extends VariableElement> parameters = queryMethod.getParameters();

        for (VariableElement methodParameter : parameters) {

            Parameter parameter = new Parameter();

            if (methodParameter.getAnnotation(ContextRef.class) != null)
                parameter.setContext(true);
            if (methodParameter.getAnnotation(Projection.class) != null)
                parameter.setProjection(true);
            if (methodParameter.getAnnotation(Selection.class) != null)
                parameter.setSelection(true);
            if (methodParameter.getAnnotation(SelectionArgs.class) != null)
                parameter.setSelectionArgs(true);
            if (methodParameter.getAnnotation(SortOrder.class) != null)
                parameter.setSortOrder(true);
            if (methodParameter.getAnnotation(ContentUri.class) != null)
                parameter.setContentUri(true);
            // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same qualified
            // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
            if (methodParameter.asType().toString().equals(mStringType.toString()))
                parameter.setString(true);

            final UriPlaceholder uriPlaceholder = methodParameter.getAnnotation(UriPlaceholder.class);

            if (uriPlaceholder != null) {

                parameter.setPlaceholderName(uriPlaceholder.value());
                parameter.setParameterType(methodParameter.asType().toString());
                parameter.setUriPlaceholderType(delegateUri.getUriPlaceholderType(uriPlaceholder.value()));
                parameter.setKeyName(delegateUri.getParameterPosition(uriPlaceholder.value()));
            }

            delegateMethod.addParameter(parameter);
        }

        delegateClass.addMethod(delegateMethod);

        return delegateMethod;
    }

    private boolean hasValidSignature(ExecutableElement method, Update query, DelegateUri uri) {

        boolean isValid = true;

        TypeMirror returnType = method.getReturnType();

        if (!ANDROID_DATABASE_CURSOR_CLASS_NAME.equals(returnType.toString())) {

            mLogger.trace(String.format("        Update %s method does not return expected Android Cursor.", method));
            mLogger.error(String.format("@Update annotated methods must return android.database.Cursor."), method);

            isValid = false;
        }

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

            accumulatedAnnotations.add(annotationClass);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                mLogger.trace(String
                        .format("        Multiple incompatible annotatoins on same parameter [%s]. Signaling compilatoin error.",
                                accumulatedAnnotations));
                mLogger.error(
                        String.format("Parameters can only be annotated with one of %s.", accumulatedAnnotations),
                        parameterElement);
            }

            // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same qualified
            // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
            if (!parameterType.toString().equals(requiredType.toString())) {

                isValid = false;

                mLogger.trace(String.format(
                        "        Parameter is not of expected type %s. Signaling compilatoin error.", requiredType,
                        parameterElement));
                mLogger.error(
                        String.format("Parameters annotated with @%s must be of type %s.",
                                annotationClass.getSimpleName(), requiredType), parameterElement);
            }
        }

        return isValid;
    }

    private boolean validateUriPlaceholderAnnotation(VariableElement parameterElement, Update query, DelegateUri uri,
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

        for (InterceptorBlueprint concreteAnnotation : metadata.getInterceptorBlueprints()) {

            mLogger.trace(String.format("        Checking for interceptor %s.", concreteAnnotation.getTypeElement()));

            List<? extends AnnotationMirror> annotationsMirrors = delegateMethod.getExecutableElement()
                    .getAnnotationMirrors();

            for (AnnotationMirror mirror : annotationsMirrors) {

                final TypeElement annotationTypeElement = concreteAnnotation.getTypeElement();

                // Eclipse issue: Can't use Types.isSameType() as types will not match (even if they have the same
                // qualified name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
                if (mirror.getAnnotationType().toString().equals(annotationTypeElement.asType().toString())) {

                    delegateMethod.addInterceptor(concreteAnnotation.createInterceptorPoint(mirror, mElementUtils,
                            mTypeUtils, mLogger));
                    mLogger.trace(String.format("        Interceptor %s added to method.",
                            concreteAnnotation.getInterceptorTypeElement()));
                }
            }
        }
    }
}
