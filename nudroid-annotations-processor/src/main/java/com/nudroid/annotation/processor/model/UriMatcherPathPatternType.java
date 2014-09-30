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

/**
 * The uri matcher patterns. "*" for string placeholders, "#" for number placeholders.
 */
public enum UriMatcherPathPatternType {
    STRING("*"), NUMBER("#");

    private final String pattern;

    UriMatcherPathPatternType(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Gets the string representation of this type pattern.
     *
     * @return "*" for string placeholder, "#" for number placeholder.
     */
    public String getPattern() {

        return pattern;
    }

    /**
     * Given a class name, get the appropriate uri matcher pattern. This method returns STRING for "java.lang.String"
     * and NUMBER for everything else.
     *
     * @param className
     *         the class name to check for
     *
     * @return the proper URI pattern for the parameter type
     */
    public static UriMatcherPathPatternType fromClass(String className) {

        String stringClassName = String.class.getName();

        if (className.equals(stringClassName)) {
            return STRING;
        } else {
            return NUMBER;
        }
    }

    /**
     * Checks whether the provided string is either a STRING or NUMBER pattern (i.e. the string equals "*" or "#").
     *
     * @param value
     *         the string to model
     *
     * @return <tt>true</tt> if the value represents a STRING or NUMBER pattern.
     */
    public static boolean isPattern(String value) {

        return value.equals(STRING.pattern) || value.equals(NUMBER.pattern);
    }
}
