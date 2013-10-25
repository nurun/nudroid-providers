package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a concrete annotation implementation that will be created.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class ConcreteAnnotation {

    private String mQualifiedName;
    private String mConcreteClassName;
    private String mSimpleName;
    private String mConcretePackageName;
    private List<AnnotationAttribute> mAttributes = new ArrayList<AnnotationAttribute>();
    private TypeElement mTypeElement;

    // TODO Finish javadoc
    public ConcreteAnnotation(TypeElement typeElement) {

        this.mTypeElement = typeElement;
        this.mQualifiedName = typeElement.getQualifiedName().toString();
        this.mSimpleName = typeElement.getSimpleName().toString();

        calculateConcreteClassAndPackageNames(typeElement);
    }

    // TODO Finish javadoc
    public void addAttribute(AnnotationAttribute attribute) {

        this.mAttributes.add(attribute);
    }

    /**
     * TODO Finish javadoc
     * @return the mTypeElement
     */
    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    /**
     * TODO Finish javadoc
     * 
     * @return the qualifiedName
     */
    public String getQualifiedName() {
        return mQualifiedName;
    }

    /**
     * TODO Finish javadoc
     * 
     * @return the simpleName
     */
    public String getSimpleName() {
        return mSimpleName;
    }

    /**
     * TODO Finish javadoc
     * 
     * @return the mConcreteClassName
     */
    public String getConcreteClassName() {
        return mConcreteClassName;
    }

    /**
     * TODO Finish javadoc
     * 
     * @return the packageName
     */
    public String getConcretePackageName() {
        return mConcretePackageName;
    }

    /**
     * TODO Finish javadoc
     * 
     * @return the attributes
     */
    public List<AnnotationAttribute> getAttributes() {
        return Collections.unmodifiableList(mAttributes);
    }
    
    private void calculateConcreteClassAndPackageNames(TypeElement typeElement) {
        
        Element element = typeElement.getEnclosingElement();
        StringBuilder concreteSimpleName = new StringBuilder(typeElement.getSimpleName());

        while (element != null && !element.getKind().equals(ElementKind.PACKAGE)) {
            
            concreteSimpleName.insert(0, "$").insert(0, element.getSimpleName());
            element = element.getEnclosingElement();
        }

        concreteSimpleName.insert(0, "Concrete");
        
        if (StringUtils.isEmpty(element.getSimpleName().toString())) {
            this.mConcretePackageName = "";
        } else {
            this.mConcretePackageName = element.toString();
        }
        
        this.mConcreteClassName = concreteSimpleName.toString();
    }
}