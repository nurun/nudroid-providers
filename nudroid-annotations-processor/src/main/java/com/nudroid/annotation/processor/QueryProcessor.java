package com.nudroid.annotation.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.nudroid.annotation.provider.delegate.Authority;
import com.nudroid.annotation.provider.delegate.ContentUri;
import com.nudroid.annotation.provider.delegate.ContentValuesRef;
import com.nudroid.annotation.provider.delegate.PathParam;
import com.nudroid.annotation.provider.delegate.Projection;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.QueryParam;
import com.nudroid.annotation.provider.delegate.Selection;
import com.nudroid.annotation.provider.delegate.SelectionArgs;
import com.nudroid.annotation.provider.delegate.SortOrder;

/**
 * Processes the Query annotations on a class.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class QueryProcessor {

    private Continuation continuation;
    private Metadata metadata;
    private ProcessingEnvironment processingEnv;
    private Elements elementUtils;
    private LoggingUtils logger;

    private TypeMirror stringType;
    private TypeMirror arrayOfStringsType;
    private TypeMirror contentValuesType;
    private TypeMirror uriType;
    private Types typeUtils;

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    QueryProcessor(ProcessorContext processorContext) {

        this.continuation = processorContext.continuation;
        this.metadata = processorContext.metadata;
        this.processingEnv = processorContext.processingEnv;
        this.elementUtils = processorContext.elementUtils;
        this.typeUtils = processorContext.typeUtils;
        this.logger = processorContext.logger;

        stringType = processorContext.elementUtils.getTypeElement("java.lang.String").asType();
        arrayOfStringsType = processorContext.typeUtils.getArrayType(stringType);
        contentValuesType = processorContext.elementUtils.getTypeElement("android.content.ContentValues").asType();
        uriType = processorContext.elementUtils.getTypeElement("android.net.Uri").asType();
    }

    /**
     * Process the queries on the given class.
     * 
     * @param typeElement
     *            The class to process.
     */
    void processQueriesOnClass(TypeElement typeElement) {

        List<? extends Element> methodElementsList = elementUtils.getAllMembers(elementUtils.getTypeElement(typeElement
                .toString()));

        for (Element targetElement : methodElementsList) {

            processQueryOnMethod(typeElement, targetElement);
        }
    }

    private void processQueryOnMethod(TypeElement rootClass, Element method) {

        Query query = method.getAnnotation(Query.class);

        if (query == null) return;

        continuation.addContinuationElement(rootClass);
        Uri uri = composeUri(rootClass, method, query);

        if (uri == null) return;

        boolean isValidAnnotations = validateAnnotations(rootClass, method, query, uri);

        if (!isValidAnnotations || ElementUtils.isAbstract(rootClass)) return;

        logger.debug(String.format("Processing Query annotation on %s.%s", rootClass, method));
        metadata.mapUri(rootClass, (ExecutableElement) method, uri);
    }

    private Uri composeUri(TypeElement rootClass, Element method, Query query) {

        final String uriPathAndQuery = query.value();
        Uri uri = null;

        try {

            final String authority = metadata.parseAuthorityFromClass(rootClass);
            uri = new Uri(authority, uriPathAndQuery, logger);
        } catch (DuplicateUriPlaceholderException e) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format("Duplicated placeholder parameter: '%s'.", e.getPlaceholderName()), method);
        } catch (IllegalUriPathException e) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format("Path '%s' is not a valid query URI.", uriPathAndQuery), method);
        }

        return uri;
    }

    private boolean validateAnnotations(TypeElement rootClass, Element method, Query query, Uri uri) {

        boolean isValid = true;

        if (ElementUtils.isInterface(rootClass)) {

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    "Java does not inherit annotations from methods. "
                            + "The @Query annotation in this method won't be inherited by implementing classes.",
                    method);

            isValid = false;
        }

        if (!validateQueryElement(rootClass, method)) isValid = false;
        if (!validateParameters(query, (ExecutableElement) method, uri)) isValid = false;
        return isValid;
    }

    private boolean validateParameters(Query query, ExecutableElement method, Uri uri) {

        boolean isValid = true;

        List<? extends VariableElement> parameters = method.getParameters();

        for (VariableElement var : parameters) {

            List<Class<?>> accumulatedAnnotations = new ArrayList<Class<?>>();

            final TypeMirror parameterType = var.asType();

            isValid = validateParameterAnnotation(var, Projection.class, parameterType, arrayOfStringsType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, Selection.class, parameterType, stringType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, SelectionArgs.class, parameterType, arrayOfStringsType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, SortOrder.class, parameterType, stringType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, ContentValuesRef.class, parameterType, contentValuesType,
                    accumulatedAnnotations);
            isValid = validateParameterAnnotation(var, ContentUri.class, parameterType, uriType, accumulatedAnnotations);

            isValid = validatePathParamAnnotation(var, query, uri, accumulatedAnnotations);
            isValid = validateQueryParamAnnotation(var, query, uri, accumulatedAnnotations);
        }

        return isValid;
    }

    private boolean validatePathParamAnnotation(VariableElement var, Query query, Uri uri,
            List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        final PathParam annotation = var.getAnnotation(PathParam.class);

        if (annotation != null) {

            String paramName = annotation.value();
            final String uriPathAndQuery = query.value();

            accumulatedAnnotations.add(PathParam.class);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Parameters %s can only be annotated with one of %s.", var,
                                accumulatedAnnotations), var);
            }

            if (!uri.containsPathPlaceholder(paramName)) {

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Could not find placeholder named '%s' on the path element of '%s'.", paramName,
                                uriPathAndQuery), var);

                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateQueryParamAnnotation(VariableElement var, Query query, Uri uri,
            List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        final QueryParam annotation = var.getAnnotation(QueryParam.class);

        if (annotation != null) {

            String paramName = annotation.value();
            final String queryPath = query.value();

            accumulatedAnnotations.add(PathParam.class);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Parameter '%s' can only be annotated with one of %s.", var,
                                accumulatedAnnotations), var);
            }

            if (!uri.containsQueryPlaceholder(paramName)) {

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Could not find placeholder named '%s' on the query string element of '%s'.",
                                paramName, queryPath), var);

                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateQueryElement(Element rootClass, Element method) {

        boolean isValid = true;
        Element enclosingElement = method.getEnclosingElement();
        Element parentEnclosingElement = enclosingElement.getEnclosingElement();

        if (!validateClassIsAnnotatedWithAuthority(method, rootClass)) {
            isValid = false;
        }

        if (!validateClassIsTopLevelOrStatic(method, enclosingElement, parentEnclosingElement)) {
            isValid = false;
        }

        if (ElementUtils.isClass(enclosingElement) && !validateClassHasDefaultConstructor(enclosingElement)) {
            isValid = false;
        }

        return isValid;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean validateParameterAnnotation(VariableElement var, Class annotationClass, TypeMirror parameterType,
            TypeMirror requiredType, List<Class<?>> accumulatedAnnotations) {

        boolean isValid = true;

        if (var.getAnnotation(annotationClass) != null) {

            accumulatedAnnotations.add(annotationClass);

            if (accumulatedAnnotations.size() > 1) {

                isValid = false;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Parameters %s can only be annotated with one of %s.", var,
                                accumulatedAnnotations), var);
            }

            if (!typeUtils.isSameType(parameterType, requiredType)) {

                isValid = false;

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format("Parameters %s annotated with %s must be of type %s.", var, annotationClass,
                                requiredType), var);
            }
        }

        return isValid;
    }

    private boolean validateClassIsAnnotatedWithAuthority(Element method, Element methodEnclosingClass) {

        boolean isValid = true;

        if (methodEnclosingClass.getAnnotation(Authority.class) == null) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Class needs to be annotated with @Authority annotation.", method);

            isValid = false;
        }

        return isValid;
    }

    private boolean validateClassIsTopLevelOrStatic(Element method, Element methodEnclosingClass,
            Element parentparentClassElement) {

        boolean isValid = true;

        if (!ElementUtils.isClassOrInterface(methodEnclosingClass)) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@Query annotations can only appear on class and interface methods.", method);

            isValid = false;
        }

        Set<Modifier> modifiers = methodEnclosingClass.getModifiers();

        if (ElementUtils.isClassOrInterface(parentparentClassElement) && !modifiers.contains(Modifier.STATIC)) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@Query annotations can only appear on top level or static classes.", method);

            isValid = false;
        }

        return isValid;
    }

    private boolean validateClassHasDefaultConstructor(Element rootClass) {

        List<? extends Element> enclosedElements = rootClass.getEnclosedElements();

        for (Element child : enclosedElements) {

            if (child.getKind().equals(ElementKind.CONSTRUCTOR)) {

                if (((ExecutableElement) child).getParameters().size() == 0) {
                    return true;
                }
            }
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "Classes annotated with content provider annotations must provide a default constructor.", rootClass);

        return false;
    }
}
