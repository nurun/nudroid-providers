/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.nurun.persistence.annotation.Authority;
import com.nurun.persistence.annotation.ContentUri;
import com.nurun.persistence.annotation.ContentValues;
import com.nurun.persistence.annotation.PathParam;
import com.nurun.persistence.annotation.Projection;
import com.nurun.persistence.annotation.Query;
import com.nurun.persistence.annotation.QueryParam;
import com.nurun.persistence.annotation.Selection;
import com.nurun.persistence.annotation.SelectionArgs;
import com.nurun.persistence.annotation.SortOrder;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
class Metadata {

    private UriRegistry uriRegistry;
    private Map<String, DelegateClass> delegateClasses = new HashMap<String, DelegateClass>();
    private Map<Integer, Set<DelegateMethod>> delegateMethods = new HashMap<Integer, Set<DelegateMethod>>();
    private Map<String, TypeElement> authorityToClass = new HashMap<String, TypeElement>();

    private Types typeUtils;
    private TypeElement stringType;
    private LoggingUtils logger;

    public Metadata(UriRegistry uriRegistry, Elements elementUtils, Types typeUtils, LoggingUtils logger) {

        this.logger = logger;
        this.typeUtils = typeUtils;
        this.uriRegistry = uriRegistry;
        this.stringType = elementUtils.getTypeElement("java.lang.String");
    }

    public TypeElement getClassForAuthority(String string) {

        return authorityToClass.get(string);
    }

    public void setClassForAuthority(String value, TypeElement typeElement) {

        authorityToClass.put(value, typeElement);
    }

    public String parseAuthorityFromClass(Element classRoot) {

        Authority authority = classRoot.getAnnotation(Authority.class);
        String authorityName = authority != null ? authority.value() : classRoot.toString();

        authorityName = authorityName.equals("") ? classRoot.toString() : authorityName;

        return authorityName;
    }

    public void mapUri(Element classElement, ExecutableElement methodExecutableElement, String authority, Query query) {

        DelegateClass delegateClass = delegateClasses.get(classElement.toString());

        if (delegateClass == null) {
            delegateClass = new DelegateClass(classElement, logger);
            delegateClasses.put(classElement.toString(), delegateClass);
        }

        Uri uri = new Uri(authority, query.value(), logger);
        int uriId = uriRegistry.addUri(uri);
        uri.setId(uriId + 1);

        DelegateMethod delegateMethod = new DelegateMethod(methodExecutableElement, uri);

        List<? extends VariableElement> parameters = methodExecutableElement.getParameters();

        for (VariableElement var : parameters) {

            Parameter parameter = new Parameter();
            parameter.setName(var.getSimpleName().toString());

            if (var.getAnnotation(Projection.class) != null) parameter.setProjection(true);
            if (var.getAnnotation(Selection.class) != null) parameter.setSelection(true);
            if (var.getAnnotation(SelectionArgs.class) != null) parameter.setSelectionArgs(true);
            if (var.getAnnotation(SortOrder.class) != null) parameter.setSortOrder(true);
            if (var.getAnnotation(ContentValues.class) != null) parameter.setContentValues(true);
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
                parameter.setQueryParameterName(uri.getQueryParameterName(queryParamAnnotation.value()));
                delegateMethod.addQueryPlaceholder(parameter.getQueryParameterName());
            }

            delegateMethod.addParameter(parameter);
        }

        delegateClass.addMethod(delegateMethod);
        addMethodDelegateToUriMapping(delegateMethod);
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

    public Map<String, DelegateClass> getDelegateClasses() {

        return Collections.unmodifiableMap(delegateClasses);
    }
}
