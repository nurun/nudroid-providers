/*
 *
 *  * Copyright (c) 2014 Nurun Inc.
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 */

package com.nudroid.annotation.processor.model;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * The uri matcher patterns. "*" for string placeholders, "#" for number placeholders.
 */
public enum ParamTypePattern {
    STRING("*"), NUMBER("#");

    private final String mPattern;

    ParamTypePattern(String pattern) {
        this.mPattern = pattern;
    }

    /**
     * Gets the string representation of this type pattern.
     *
     * @return "*" for string placeholder, "#" for number placeholder.
     */
    public String getPattern() {

        return mPattern;
    }

    /**
     * Given the parameter type, get the appropriate uri matcher patters.
     *
     * @param mirror
     *         The type of the parameter to map to the placeholder.
     * @param elementUtils
     * @param typeUtils
     *
     * @return The proper URI pattern for the parameter type.
     */
    public static ParamTypePattern fromTypeMirror(TypeMirror mirror, Elements elementUtils, Types typeUtils) {

        TypeMirror stringType = elementUtils.getTypeElement(String.class.getName())
                .asType();

        if (typeUtils.isSameType(stringType, mirror)) {
            return STRING;
        } else {
            return NUMBER;
        }
    }
}
