package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

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

    /**
     * Creates a new bean.
     * 
     * @param typeElement
     *            The {@link TypeElement} for the annotation this class represents.
     */
    public ConcreteAnnotation(TypeElement typeElement) {

        this.mTypeElement = typeElement;
        this.mAnnotationQualifiedName = typeElement.getQualifiedName().toString();
        this.mAnnotationSimpleName = typeElement.getSimpleName().toString();

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
}