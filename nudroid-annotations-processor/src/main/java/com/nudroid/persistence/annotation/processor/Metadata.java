package com.nudroid.persistence.annotation.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.nudroid.persistence.annotation.Authority;
import com.nudroid.persistence.annotation.ContentUri;
import com.nudroid.persistence.annotation.ContentValuesRef;
import com.nudroid.persistence.annotation.PathParam;
import com.nudroid.persistence.annotation.Projection;
import com.nudroid.persistence.annotation.Query;
import com.nudroid.persistence.annotation.QueryParam;
import com.nudroid.persistence.annotation.Selection;
import com.nudroid.persistence.annotation.SelectionArgs;
import com.nudroid.persistence.annotation.SortOrder;

/**
 * Gather all the information required to generate the source code for the router files and uri registry.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Metadata {

    private UriRegistry uriRegistry;
    private Map<String, DelegateClass> delegateClasses = new HashMap<String, DelegateClass>();
    private Map<Integer, Set<DelegateMethod>> delegateMethods = new HashMap<Integer, Set<DelegateMethod>>();
    private Map<String, TypeElement> authorityToClass = new HashMap<String, TypeElement>();

    private Types typeUtils;
    private TypeElement stringType;
    private LoggingUtils logger;

    /**
     * Creates an instance of this class.
     * 
     * @param elementUtils
     *            The annotation processor's {@link Elements} instance.
     * @param typeUtils
     *            The annotation processor's {@link Types} instance.
     * @param logger
     *            The logger utility instance to use.
     */
    Metadata(Elements elementUtils, Types typeUtils, LoggingUtils logger) {

        this.logger = logger;
        this.typeUtils = typeUtils;
        this.uriRegistry = new UriRegistry();
        this.stringType = elementUtils.getTypeElement("java.lang.String");
    }

    /**
     * Maps a delegate class to a <a
     * href="http://developer.android.com/reference/android/content/ContentProvider.html">ContentProvider</a> authority
     * name.
     * 
     * @param authority
     *            The authority name.
     * @param typeElement
     *            The type element for the delaget class.
     */
    void setClassForAuthority(String authority, TypeElement typeElement) {

        authorityToClass.put(authority, typeElement);
    }

    /**
     * Gets the delegate class associated with the given <a
     * href="http://developer.android.com/reference/android/content/ContentProvider.html">ContentProvider</a> authority.
     * 
     * @param authority
     *            The authority name.
     * 
     * @return The {@link TypeElement} for the delegate class associated with the given authority name.
     */
    TypeElement getClassForAuthority(String authority) {

        return authorityToClass.get(authority);
    }

    /**
     * Get's the authority name from the authority annotation. Infer the authority name from the class name if an
     * explicit authority name is not given to the annotation.
     * 
     * @param delegateClass
     *            The class to get the authority name for.
     * 
     * @return The name of the authority the delegate class handles.
     * 
     * @throws IllegalArgumentException
     *             if the class is not annotated with {@link Authority}.
     */
    String parseAuthorityFromClass(TypeElement delegateClass) {

        Authority authority = delegateClass.getAnnotation(Authority.class);

        if (authority == null) {

            throw new IllegalArgumentException(String.format("Delegate class %s must be annotated with %s.",
                    delegateClasses.toString(), Authority.class.getName()));
        }

        String authorityName = authority != null ? authority.value() : delegateClass.toString();

        authorityName = authorityName.equals("") ? delegateClass.toString() : authorityName;

        return authorityName;
    }

    /**
     * Maps a <a href="http://developer.android.com/reference/android/content/ContentProvider.html">ContentProvider</a>
     * URI with it's target delegate class and {@link Query} method.
     * <p/>
     * The mapping will be stored in this metadata and can be retrieved by the several getXXX methods available.
     * 
     * @param delegateClassType
     *            The type of the class responsible for handling this URI.
     * @param delegateMethodType
     *            The delegate method within the delegate class to handle this particular URI.
     * @param uri
     *            The URI to map.
     */
    void mapUri(TypeElement delegateClassType, ExecutableElement delegateMethodType, Uri uri) {

        DelegateClass delegateClass = delegateClasses.get(delegateClassType.toString());

        if (delegateClass == null) {
            delegateClass = new DelegateClass(delegateClassType, logger);
            delegateClasses.put(delegateClassType.toString(), delegateClass);
        }

        int uriId = uriRegistry.addUri(uri);
        uri.setId(uriId + 1);

        DelegateMethod delegateMethod = new DelegateMethod(delegateMethodType, uri);
        delegateMethod.setQueryParameterNames(uri.getQueryParameterNames());

        List<? extends VariableElement> parameters = delegateMethodType.getParameters();

        for (VariableElement var : parameters) {

            Parameter parameter = new Parameter();

            if (var.getAnnotation(Projection.class) != null) parameter.setProjection(true);
            if (var.getAnnotation(Selection.class) != null) parameter.setSelection(true);
            if (var.getAnnotation(SelectionArgs.class) != null) parameter.setSelectionArgs(true);
            if (var.getAnnotation(SortOrder.class) != null) parameter.setSortOrder(true);
            if (var.getAnnotation(ContentValuesRef.class) != null) parameter.setContentValues(true);
            if (var.getAnnotation(ContentUri.class) != null) parameter.setContentUri(true);
            if (typeUtils.isSameType(var.asType(), stringType.asType())) parameter.setString(true);

            final PathParam pathParamAnnotation = var.getAnnotation(PathParam.class);

            if (pathParamAnnotation != null) {

                parameter.setPathParameter(true);
                parameter.setPathParamPosition(uri.getPathParameterPosition(pathParamAnnotation.value()));
                delegateMethod.addPathPlaceholder(pathParamAnnotation.value());
            }

            final QueryParam queryParamAnnotation = var.getAnnotation(QueryParam.class);

            if (queryParamAnnotation != null) {

                parameter.setQueryParameter(true);
                parameter.setQueryParameterName(uri.getQueryParameterPlaceholderName(queryParamAnnotation.value()));
            }

            delegateMethod.addParameter(parameter);
        }

        delegateClass.addMethod(delegateMethod);
        addMethodDelegateToUriMapping(delegateMethod);
    }

    /**
     * Returns a map with the delegate class names registered in this metadata object and their corresponding
     * {@link DelegateClass} representation.
     * 
     * @return A map with the delegate class names registered in this metadata object and their corresponding
     *         {@link DelegateClass} representation.
     */
    Map<String, DelegateClass> getDelegateClasses() {

        return Collections.unmodifiableMap(delegateClasses);
    }

    /**
     * Returns a list of unique URIs handled by all the delegate classes.
     * 
     * @return A list of unique URIs handled by all the delegate classes.
     */
    List<Uri> getUniqueUris() {
        return uriRegistry.getUniqueUris();
    }

    private void addMethodDelegateToUriMapping(DelegateMethod delegateMethod) {

        Set<DelegateMethod> methodSet = delegateMethods.get(delegateMethod.getUriId());

        if (methodSet == null) {
            methodSet = new HashSet<DelegateMethod>();
            methodSet.add(delegateMethod);
            delegateMethods.put(delegateMethod.getUriId(), methodSet);
        } else {

            methodSet.add(delegateMethod);
        }
    }

    /**
     * Keeps a record of unique URIs. A unique URI is identified by it's autority + path elements and does not take into
     * consideration the query string. This means that the URIs
     * <tt>content://authority/users/{name}?orderBy={field}</tt> and
     * <tt>content://authority/users/{name}?model={model_type}</tt> are considered to be the same.
     * 
     * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
     */
    private static class UriRegistry {

        List<Uri> uris = new ArrayList<Uri>();

        int addUri(Uri uri) {

            int existing = uris.indexOf(uri);

            if (existing == -1) {
                uris.add(uri);
            }

            return uris.indexOf(uri);
        }

        List<Uri> getUniqueUris() {
            return Collections.unmodifiableList(uris);
        }

        @Override
        public String toString() {
            return "UriRegistry [uris=" + uris + "]";
        }
    }
}
