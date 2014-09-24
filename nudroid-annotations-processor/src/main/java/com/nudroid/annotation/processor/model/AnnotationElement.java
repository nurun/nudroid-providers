/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.nudroid.annotation.processor.model;

import com.nudroid.annotation.processor.UsedBy;

import javax.lang.model.element.ExecutableElement;

/**
 * A Java annotation element.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class AnnotationElement {

    private String type;
    private String name;
    private String capitalizedName;

    private AnnotationElement(String type, String name, String capitalizedName) {

        this.type = type;
        this.name = name;
        this.capitalizedName = capitalizedName;
    }

    /**
     * Gets the return type of this annotation element.
     *
     * @return the type of this annotation attribute
     */
    @UsedBy("ConcreteAnnotationTemplate.stg")
    public String getType() {

        return type;
    }

    /**
     * Gets the name of this element.
     *
     * @return the name of the element
     */
    @UsedBy("ConcreteAnnotationTemplate.stg")
    public String getName() {

        return name;
    }

    /**
     * Convenience method for returning the element name with its first character in upper case.
     *
     * @return the name of this element with its first letter in upper case
     */
    @UsedBy("ConcreteAnnotationTemplate.stg")
    public String getCapitalizedName() {

        return capitalizedName;
    }

    /**
     * Builder for AnnotationElements.
     */
    public static class Builder {

        private final String type;
        private final String name;
        private final String capitalizedName;

        /**
         * Initializes the builder.
         *
         * @param annotationMethod
         *         the annotation model's method to get the metadata from
         */
        public Builder(ExecutableElement annotationMethod) {

            this.type = annotationMethod.getReturnType()
                    .toString();

            final String methodName = annotationMethod.getSimpleName()
                    .toString();
            this.name = methodName;
            this.capitalizedName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
        }

        /**
         * Creates the AnnotationAttribute instance.
         *
         * @return a new instance of AnnotationElement
         */
        public AnnotationElement build() {

            return new AnnotationElement(this.type, this.name, this.capitalizedName);
        }
    }
}
