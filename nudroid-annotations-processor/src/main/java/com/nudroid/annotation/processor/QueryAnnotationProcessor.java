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
import com.nudroid.annotation.provider.delegate.ContentProviderDelegate;
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
class QueryAnnotationProcessor {

	private LoggingUtils mLogger;

	private TypeMirror mStringType;
	private TypeMirror mArrayOfStringsType;
	private TypeMirror mContentValuesType;
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

		mStringType = processorContext.elementUtils.getTypeElement("java.lang.String").asType();
		mArrayOfStringsType = processorContext.typeUtils.getArrayType(mStringType);
		mContentValuesType = processorContext.elementUtils.getTypeElement("android.content.ContentValues").asType();
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
	public void processQueryAnnotationOnMethods(RoundEnvironment roundEnv, Metadata metadata) {

		Set<ExecutableElement> queryMethods = (Set<ExecutableElement>) roundEnv.getElementsAnnotatedWith(Query.class);

		mLogger.info("Start processing @Query annotations.");
		mLogger.trace("    Methods annotated with @Query for the round " + queryMethods);

		for (ExecutableElement queryMethod : queryMethods) {

			TypeElement enclosingClass = (TypeElement) queryMethod.getEnclosingElement();
			ContentProviderDelegate contentProviderDelegateAnnotation = enclosingClass
			        .getAnnotation(ContentProviderDelegate.class);

			if (contentProviderDelegateAnnotation == null) {

				mLogger.error(
				        String.format("Enclosing class must be annotated with @%s",
				                ContentProviderDelegate.class.getName()), queryMethod);
				continue;
			}

			mLogger.trace("    Processing " + queryMethod);
			DelegateClass delegateClass = metadata.getDelegateClassForTypeElement(enclosingClass);
			processQueryOnMethod(enclosingClass, queryMethod, delegateClass);
			mLogger.trace("    Done processing " + queryMethod);
		}

		mLogger.info("Done processing @Query annotations.");
	}

	private void processQueryOnMethod(TypeElement enclosingClass, ExecutableElement queryMethod,
	        DelegateClass delegateClass) {

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
			mLogger.error(String.format("An equivalent path has already been registered by method %s",
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

			if (methodParameter.getAnnotation(Projection.class) != null) parameter.setProjection(true);
			if (methodParameter.getAnnotation(Selection.class) != null) parameter.setSelection(true);
			if (methodParameter.getAnnotation(SelectionArgs.class) != null) parameter.setSelectionArgs(true);
			if (methodParameter.getAnnotation(SortOrder.class) != null) parameter.setSortOrder(true);
			if (methodParameter.getAnnotation(ContentValuesRef.class) != null) parameter.setContentValues(true);
			if (methodParameter.getAnnotation(ContentUri.class) != null) parameter.setContentUri(true);
			if (mTtypeUtils.isSameType(methodParameter.asType(), mStringType)) parameter.setString(true);

			final PathParam pathParamAnnotation = methodParameter.getAnnotation(PathParam.class);

			if (pathParamAnnotation != null) {

				parameter.setPathParameter(true);
				parameter.setPathParamPosition(delegateUri.getPathParameterPosition(pathParamAnnotation.value()));
				delegateMethod.addPathPlaceholder(pathParamAnnotation.value());
			}

			final QueryParam queryParamAnnotation = methodParameter.getAnnotation(QueryParam.class);

			if (queryParamAnnotation != null) {

				parameter.setQueryParameter(true);
				parameter.setQueryParameterName(delegateUri.getQueryParameterPlaceholderName(queryParamAnnotation
				        .value()));
			}

			delegateMethod.addParameter(parameter);
		}

		delegateClass.addMethod(delegateMethod);
	}

	private boolean hasValidAnnotations(TypeElement enclosingClass, ExecutableElement method, Query query,
	        DelegateUri uri) {

		boolean isValid = true;

		List<? extends VariableElement> parameters = method.getParameters();

		for (VariableElement parameterElement : parameters) {

			List<Class<?>> accumulatedAnnotations = new ArrayList<Class<?>>();

			final TypeMirror parameterType = parameterElement.asType();

			isValid = validateParameterAnnotation(parameterElement, Projection.class, parameterType,
			        mArrayOfStringsType, accumulatedAnnotations);
			isValid = validateParameterAnnotation(parameterElement, Selection.class, parameterType, mStringType,
			        accumulatedAnnotations);
			isValid = validateParameterAnnotation(parameterElement, SelectionArgs.class, parameterType,
			        mArrayOfStringsType, accumulatedAnnotations);
			isValid = validateParameterAnnotation(parameterElement, SortOrder.class, parameterType, mStringType,
			        accumulatedAnnotations);
			isValid = validateParameterAnnotation(parameterElement, ContentValuesRef.class, parameterType,
			        mContentValuesType, accumulatedAnnotations);
			isValid = validateParameterAnnotation(parameterElement, ContentUri.class, parameterType, mUriType,
			        accumulatedAnnotations);

			isValid = validatePathParamAnnotation(parameterElement, query, uri, accumulatedAnnotations);
			isValid = validateQueryParamAnnotation(parameterElement, query, uri, accumulatedAnnotations);
		}

		return isValid;
	}

	// private boolean validateQueryElement(TypeElement rootClass, ExecutableElement method) {
	//
	// boolean isValid = true;
	// Element enclosingElement = method.getEnclosingElement();
	// Element parentEnclosingElement = enclosingElement.getEnclosingElement();
	//
	// if (!validateClassIsAnnotatedWithAuthority(method, rootClass)) {
	// isValid = false;
	// }
	//
	// if (!validateClassIsTopLevelOrStatic(method, enclosingElement, parentEnclosingElement)) {
	// isValid = false;
	// }
	//
	// if (ElementUtils.isClass(enclosingElement) && !validateClassHasDefaultConstructor(enclosingElement)) {
	// isValid = false;
	// }
	//
	// return isValid;
	// }

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

	private boolean validatePathParamAnnotation(VariableElement parameterElement, Query query, DelegateUri uri,
	        List<Class<?>> accumulatedAnnotations) {

		boolean isValid = true;

		final PathParam annotation = parameterElement.getAnnotation(PathParam.class);

		if (annotation != null) {

			mLogger.trace(String.format("        Validating annotation @%s on prameter %s",
			        PathParam.class.getSimpleName(), parameterElement));

			String placeholderName = annotation.value();
			final String uriPathAndQuery = query.value();

			accumulatedAnnotations.add(PathParam.class);

			if (accumulatedAnnotations.size() > 1) {

				isValid = false;

				mLogger.trace(String
				        .format("        Multiple annotatoins on same parameter. Signaling compilatoin error."));
				mLogger.error(
				        String.format("Parameters can only be annotated with one of %s.", accumulatedAnnotations),
				        parameterElement);
			}

			if (!uri.containsPathPlaceholder(placeholderName)) {

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

	private boolean validateQueryParamAnnotation(VariableElement parameterElement, Query query, DelegateUri uri,
	        List<Class<?>> accumulatedAnnotations) {

		boolean isValid = true;

		final QueryParam annotation = parameterElement.getAnnotation(QueryParam.class);

		if (annotation != null) {

			mLogger.trace(String.format("        Validating annotation @%s on prameter %s",
			        QueryParam.class.getSimpleName(), parameterElement));
			String placeholderName = annotation.value();
			final String uriPathAndQuery = query.value();

			accumulatedAnnotations.add(QueryParam.class);

			if (accumulatedAnnotations.size() > 1) {

				isValid = false;

				mLogger.trace(String
				        .format("        Multiple annotatoins on same parameter. Signaling compilatoin error."));
				mLogger.error(
				        String.format("Parameters can only be annotated with one of %s.", accumulatedAnnotations),
				        parameterElement);
			}

			if (!uri.containsQueryPlaceholder(placeholderName)) {

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

	// private boolean validateQueryElement(TypeElement rootClass, ExecutableElement method) {
	//
	// boolean isValid = true;
	// Element enclosingElement = method.getEnclosingElement();
	// Element parentEnclosingElement = enclosingElement.getEnclosingElement();
	//
	// if (!validateClassIsAnnotatedWithAuthority(method, rootClass)) {
	// isValid = false;
	// }
	//
	// if (!validateClassIsTopLevelOrStatic(method, enclosingElement, parentEnclosingElement)) {
	// isValid = false;
	// }
	//
	// if (ElementUtils.isClass(enclosingElement) && !validateClassHasDefaultConstructor(enclosingElement)) {
	// isValid = false;
	// }
	//
	// return isValid;
	// }
}
