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

/**
 * Represents an interceptor annotation parameter.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class InterceptorAnnotationParameter {

    private final String literalValue;
    private final boolean isString;

    /**
     * Creates an interceptor annotation parameter.
     *
     * @param literalValue
     *         The literal value for this parameter, as it appears in the source code.
     * @param parameterType
     *         The type of this parameter.
     */
    public InterceptorAnnotationParameter(String literalValue, Class<?> parameterType) {

        this.literalValue = literalValue;

        isString = parameterType.equals(String.class) || parameterType.equals(String[].class);
    }

    /**
     * Gets the literal representation of this parameter (as it appears in the source code).
     *
     * @return The literal representation of this parameter (as it appears in the source code).
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getLiteralValue() {
        return literalValue;
    }

    /**
     * Checks if this parameter is a String or an array of Strings.
     *
     * @return <tt>true</tt> if this parameter is a String or array of Strings, <tt>false</tt> otherwise
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean isString() {

        return isString;
    }
}
