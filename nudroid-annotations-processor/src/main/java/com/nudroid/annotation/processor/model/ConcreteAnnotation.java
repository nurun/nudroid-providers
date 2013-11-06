package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import com.google.common.base.Joiner;

/**
 * Represents a concrete annotation implementation that will be created.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class ConcreteAnnotation {

    private String mAnnotationQualifiedName;
    private String mConcreteClassName;
    private String mAnnotationSimpleName;
    private String mConcretePackageName;
    private List<AnnotationAttribute> mAttributes = new ArrayList<AnnotationAttribute>();
    private TypeElement mTypeElement;
    private TypeElement mInterceptorTypeElement;
    private Interceptor mInterceptor;

    /**
     * Creates a new bean.
     * 
     * @param typeElement
     *            The {@link TypeElement} for the annotation this class represents.
     */
    public ConcreteAnnotation(TypeElement typeElement, Elements elements) {

        this.mTypeElement = typeElement;
        this.mInterceptorTypeElement = (TypeElement) typeElement.getEnclosingElement();
        this.mAnnotationQualifiedName = typeElement.getQualifiedName().toString();
        this.mAnnotationSimpleName = typeElement.getSimpleName().toString();
        this.mInterceptor = createInterceptorWithAnnotationLiterals(this, elements);

        calculateConcreteClassAndPackageNames(typeElement);
    }

    /**
     * Adds an attribute to this annotation representation.
     * 
     * @param attribute
     *            The attribute to add.
     */
    public void addAttribute(AnnotationAttribute attribute) {

        this.mAttributes.add(attribute);
    }

    /**
     * Gets the {@link TypeElement} of the annotation.
     * 
     * @return The {@link TypeElement} of the annotation.
     */
    public TypeElement getTypeElement() {

        return mTypeElement;
    }

    /**
     * Gets the interceptor's {@link TypeElement} this annotation is for.
     * 
     * @return The {@link TypeElement} of the interceptor implementation.
     */
    public TypeElement getInterceptorTypeElement() {

        return mInterceptorTypeElement;
    }

    /**
     * Gets the qualified name of the annotation class.
     * 
     * @return The qualified name of the annotation class.
     */
    public String getAnnotationQualifiedName() {
        return mAnnotationQualifiedName;
    }

    /**
     * Gets the simple name (i.e. without package name) of the annotation class.
     * 
     * @return The simple name (i.e. without package name) of the annotation class.
     */
    public String getAnnotationSimpleName() {
        return mAnnotationSimpleName;
    }

    /**
     * Gets the name of the concrete annotation implementation class.
     * 
     * @return The name of the concrete annotation implementation class.
     */
    public String getConcreteClassName() {
        return mConcreteClassName;
    }

    /**
     * Gets the package name of the concrete annotation implementation class.
     * 
     * @return The package name of the concrete annotation implementation class.
     */
    public String getConcretePackageName() {
        return mConcretePackageName;
    }

    /**
     * Gets the attributes of this concrete annotation.
     * 
     * @return The attributes of this concrete annotation.
     */
    public List<AnnotationAttribute> getAttributes() {

        return mAttributes;
    }

    /**
     * Gets the Interceptor metadata for this concrete annotation.
     * 
     * @return The Interceptor metadata for this concrete annotation.
     */
    public Interceptor getInterceptor() {
        return mInterceptor;
    }

    private void calculateConcreteClassAndPackageNames(TypeElement typeElement) {

        Element parentElement = typeElement.getEnclosingElement();
        StringBuilder concreteSimpleName = new StringBuilder(typeElement.getSimpleName());

        while (parentElement != null && !parentElement.getKind().equals(ElementKind.PACKAGE)) {

            concreteSimpleName.insert(0, "$").insert(0, parentElement.getSimpleName());
            parentElement = parentElement.getEnclosingElement();
        }

        concreteSimpleName.insert(0, "Concrete");

        if (parentElement != null && parentElement.getKind().equals(ElementKind.PACKAGE)) {

            this.mConcretePackageName = ((PackageElement) parentElement).getQualifiedName().toString();
        } else {

            this.mConcretePackageName = "";
        }

        this.mConcreteClassName = concreteSimpleName.toString();
    }

    private Interceptor createInterceptorWithAnnotationLiterals(ConcreteAnnotation concreteAnnotation,
            AnnotationMirror mirror, Elements elements) {

        Interceptor interceptor = new Interceptor(concreteAnnotation);

        SortedSet<ExecutableElement> methodKeys = new TreeSet<ExecutableElement>(new Comparator<ExecutableElement>() {

            @Override
            public int compare(ExecutableElement o1, ExecutableElement o2) {

                return o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString());
            }
        });

        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues = elements
                .getElementValuesWithDefaults(mirror);
        methodKeys.addAll(annotationValues.keySet());

        for (ExecutableElement keyEntry : methodKeys) {

            ExecutableElement attribute = keyEntry;
            AnnotationValue attributeValue = annotationValues.get(keyEntry);

            generateAnnotationLiteral(interceptor, attribute, attributeValue);
        }

        return interceptor;
    }

    private void generateAnnotationLiteral(Interceptor interceptor, ExecutableElement attribute,
            AnnotationValue attributeValue) {
        final TypeKind kind = attribute.getReturnType().getKind();

        switch (kind) {
        case ARRAY:

            generateAnnotationArrayLiteral(interceptor, attribute, attributeValue);

            break;
        case CHAR:

            interceptor.addConcreteAnnotationConstructorLiteral(String.format("'%s'", attributeValue.getValue()));
            break;
        case FLOAT:

            interceptor.addConcreteAnnotationConstructorLiteral(String.format("%sf", attributeValue.getValue()));
            break;
        case LONG:

            interceptor.addConcreteAnnotationConstructorLiteral(String.format("%sL", attributeValue.getValue()));
            break;

        case DECLARED:

            TypeElement stringType = mElementUtils.getTypeElement(String.class.getName());

            if (mTypeUtils.isSameType(stringType.asType(), attribute.getReturnType())) {

                interceptor.addConcreteAnnotationConstructorLiteral(String.format("\"%s\"", attributeValue.getValue()));
            } else {

                final Element asElement = mTypeUtils.asElement(attribute.getReturnType());

                if (asElement.getKind() == ElementKind.ENUM) {
                    interceptor.addConcreteAnnotationConstructorLiteral(String.format("%s.%s", asElement,
                            attributeValue.getValue()));
                } else {

                    mLogger.error(String.format("Invalid type %s for the annotation attribute "
                            + "%s; only primitive type, String and enumeration are permitted or 1-dimensional arrays"
                            + " thereof.", asElement, attribute), attribute);
                }
            }

            break;

        default:
            interceptor.addConcreteAnnotationConstructorLiteral(String.format("%s", attributeValue.getValue()));
        }
    }

    private void generateAnnotationArrayLiteral(Interceptor interceptor, ExecutableElement attribute,
            AnnotationValue attributeValue) {
        ArrayType arrayType = (ArrayType) attribute.getReturnType();

        @SuppressWarnings("unchecked")
        List<? extends AnnotationValue> annotationValues = (List<? extends AnnotationValue>) attributeValue.getValue();
        List<Object> arrayElements = new ArrayList<Object>();

        for (AnnotationValue value : annotationValues) {

            arrayElements.add(value.getValue());
        }

        StringBuilder arrayInitializer = new StringBuilder();

        /*
         * On Eclipse, the toString() method of the AnnotationValues of an array is not proper. It does not print the
         * fully qualified name of enum, does not surround characters and strings wiith ' and " and does not append 'f'
         * or 'l' for floats or longs. Thus the need to recreate the logic for proper source code generation.
         */
        switch (arrayType.getComponentType().getKind()) {
        case CHAR:

            arrayInitializer.append("new char[] { '");
            Joiner.on("', '").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append("' }");
            break;
        case FLOAT:

            arrayInitializer.append("new float[] { ");
            Joiner.on("f, ").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append("f }");
            break;
        case DOUBLE:

            arrayInitializer.append("new double[] { ");
            Joiner.on(", ").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append(" }");
            break;
        case INT:

            arrayInitializer.append("new int[] { ");
            Joiner.on(", ").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append(" }");
            break;
        case LONG:

            arrayInitializer.append("new long[] { ");
            Joiner.on("L, ").skipNulls().appendTo(arrayInitializer, arrayElements);
            arrayInitializer.append("L }");
            break;

        case DECLARED:

            TypeElement stringType = mElementUtils.getTypeElement("java.lang.String");

            if (mTypeUtils.isSameType(stringType.asType(), arrayType.getComponentType())) {

                arrayInitializer.append("new String[] { \"");
                Joiner.on("\", \"").skipNulls().appendTo(arrayInitializer, arrayElements);
                arrayInitializer.append("\" }");
            } else {

                final Element arrayElementType = mTypeUtils.asElement(arrayType.getComponentType());

                if (arrayElementType.getKind() == ElementKind.ENUM) {

                    arrayInitializer.append(String.format("new %s[] { %s.", arrayElementType, arrayElementType));
                    Joiner.on(String.format(", %s.", arrayElementType)).skipNulls()
                            .appendTo(arrayInitializer, arrayElements);
                    arrayInitializer.append(" }");
                } else {

                    mLogger.error(String.format("Invalid type %s for the annotation attribute "
                            + "%s; only primitive type, String and enumeration are permitted or 1-dimensional arrays"
                            + " thereof.", arrayElementType, attribute), attribute);
                }
            }

            break;
        default:
            break;
        }

        interceptor.addConcreteAnnotationConstructorLiteral(arrayInitializer.toString());
    }
}