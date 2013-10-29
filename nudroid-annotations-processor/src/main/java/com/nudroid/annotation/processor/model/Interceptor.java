package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

/**
 * TODO This class needs major rethinking. Check switch cases below. Information about delegate method interceptors.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Interceptor {

	private TypeElement mInterceptorClassElement;
	private TypeElement mInterceptorAnnotationElement;
	private boolean mHasCustomConstructor;
	private boolean mHasDefaultConstrucotr;
	private ConcreteAnnotation mConcreteAnnotation;
	private List<String> mConcreteAnnotationConstructorArguments = new ArrayList<String>();

	/**
	 * TODO correct javadoc Creates a new interceptor. TODO Remove the logic from the constructor and place it in the
	 * processor to avoid passing in the typeutils?
	 * 
	 * @param interceptorAnnotationElement
	 * @param interceptorClassElement
	 *            The type element for this interceptor.
	 * @param typeUtils
	 * @param mTypeUtils
	 */
	public Interceptor(TypeElement interceptorAnnotationElement, TypeElement interceptorClassElement, Types typeUtils,
	        ConcreteAnnotation concreteAnnotation) {

		this.mInterceptorAnnotationElement = interceptorAnnotationElement;
		this.mInterceptorClassElement = interceptorClassElement;
		this.mConcreteAnnotation = concreteAnnotation;

		List<ExecutableElement> constructors = ElementFilter.constructorsIn(interceptorClassElement
		        .getEnclosedElements());

		for (ExecutableElement constructor : constructors) {

			final List<? extends VariableElement> parameters = constructor.getParameters();

			if (parameters.size() == 0) {
				this.mHasDefaultConstrucotr = true;
			}

			if (parameters.size() == 1
			        && typeUtils.isSameType(parameters.get(0).asType(), interceptorAnnotationElement.asType())) {

				this.mHasCustomConstructor = true;
			}
		}
	}

	/**
	 * Gets the fully qualified name of the interceptor class.
	 * 
	 * @return The fully qualified name of the interceptor class.
	 */
	public String getQualifiedName() {

		return mInterceptorClassElement.getQualifiedName().toString();
	}

	/**
	 * Gets the simple name of the interceptor class.
	 * 
	 * @return The simple name of the interceptor class.
	 */
	public String getSimpleName() {

		return mInterceptorClassElement.getSimpleName().toString();
	}

	/**
	 * Gets the annotation for this interceptor.
	 * 
	 * @return The annotation for this interceptor.
	 */
	public TypeElement getInterceptorAnnotationElement() {
		return mInterceptorAnnotationElement;
	}

	/**
	 * TODO finish javadoc
	 * 
	 * @return the mHasCustomConstrucotr
	 */
	public boolean hasDefaultConstructor() {

		return mHasDefaultConstrucotr;
	}

	/**
	 * TODO finish javadoc
	 * 
	 * @return the mHasCustomConstrucotr
	 */
	public boolean hasCustomConstructor() {

		return mHasCustomConstructor;
	}

	/**
	 * @param attribute
	 * @param attributeValue
	 */
	public void addAnnotationValue(String value) {

		mConcreteAnnotationConstructorArguments.add(value);
	}

	/**
	 * TODO Dinish javadoc
	 * 
	 * @return
	 */
	public String getConcreteAnnotationQualifiedName() {

		return mConcreteAnnotation.getConcreteClassName();
	}

	/**
	 * TODO Finishs javadoc<br>
	 * 
	 * @return
	 */
	/*
	 * Important. Velocity call this method each time it needs to iterate the array, creating a new unmodifiable
	 * instance everytime. Change this here and in other methods.
	 */
	public List<String> getConcreteAnnotationConstructorArgumentLiterals() {

		return Collections.unmodifiableList(mConcreteAnnotationConstructorArguments);
	}
}
