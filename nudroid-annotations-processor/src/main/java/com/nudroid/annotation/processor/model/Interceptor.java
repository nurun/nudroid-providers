package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
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
    public void addAnnotationValue(ExecutableElement attribute, AnnotationValue attributeValue, Elements elementUtils,
            Types typeUtils) {

        final TypeKind kind = attribute.getReturnType().getKind();

        System.out.println("**** Type is " + kind);
        System.out.println("**** Value is " + attributeValue);

        switch (kind) {
        case ARRAY:

            ArrayType typeMirror = (ArrayType) attribute.getReturnType();

            switch (typeMirror.getComponentType().getKind()) {
            case CHAR:

                mConcreteAnnotationConstructorArguments
                        .add(String.format("new char[] {%s}", attributeValue.getValue()));
                break;
            case FLOAT:

                mConcreteAnnotationConstructorArguments
                        .add(String.format("new float[] {%s}", attributeValue.getValue()));
                break;
            case DOUBLE:

                mConcreteAnnotationConstructorArguments.add(String.format("new double[] {%s}",
                        attributeValue.getValue()));
                break;
            case INT:

                mConcreteAnnotationConstructorArguments.add(String.format("new int[] {%s}", attributeValue.getValue()));
                break;
            case LONG:

                mConcreteAnnotationConstructorArguments
                        .add(String.format("new long[] {%s}", attributeValue.getValue()));
                break;

            case DECLARED:

                TypeElement stringType = elementUtils.getTypeElement("java.lang.String");

                if (typeUtils.isSameType(stringType.asType(), typeMirror.getComponentType())) {

                    mConcreteAnnotationConstructorArguments.add(String.format("new String[] {%s}",
                            attributeValue.getValue()));
                } else {

                    final Element asElement = typeUtils.asElement(typeMirror.getComponentType());

                    if (asElement.getKind() == ElementKind.ENUM) {
                        mConcreteAnnotationConstructorArguments.add(String.format("new %s[] {%s}", asElement,
                                attributeValue.getValue()));
                    }
                }

                break;
            default:
                break;
            }

            break;
        case CHAR:

            mConcreteAnnotationConstructorArguments.add(String.format("'%s'", attributeValue.getValue()));
            break;
        case FLOAT:

            mConcreteAnnotationConstructorArguments.add(String.format("%sf", attributeValue.getValue()));
            break;
        case LONG:

            mConcreteAnnotationConstructorArguments.add(String.format("%sL", attributeValue.getValue()));
            break;

        case DECLARED:

            TypeElement stringType = elementUtils.getTypeElement(String.class.getName());

            if (typeUtils.isSameType(stringType.asType(), attribute.getReturnType())) {

                mConcreteAnnotationConstructorArguments.add(String.format("\"%s\"", attributeValue.getValue()));
            } else {

                final Element asElement = typeUtils.asElement(attribute.getReturnType());

                if (asElement.getKind() == ElementKind.ENUM) {
                    mConcreteAnnotationConstructorArguments.add(String.format("%s.%s", asElement,
                            attributeValue.getValue()));
                }
            }

            break;

        default:
            mConcreteAnnotationConstructorArguments.add(String.format("%s", attributeValue.getValue()));
        }
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

        System.out.println(mConcreteAnnotationConstructorArguments);
        return Collections.unmodifiableList(mConcreteAnnotationConstructorArguments);
    }
}
