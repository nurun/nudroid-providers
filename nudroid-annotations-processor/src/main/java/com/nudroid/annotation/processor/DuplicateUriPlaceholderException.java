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

package com.nudroid.annotation.processor;

/**
 * Exception raised when the same placeholder name is used more than one on a URI path and query string combination.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DuplicateUriPlaceholderException extends IllegalUriPathException {

    private static final long serialVersionUID = -3066482963147107085L;
    private final String duplicatePosition;
    private final String existingPosition;
    private final String placeholderName;

    /**
     * Creates an instance of this class.
     *
     * @param placeholderName
     *         The name of the placeholder in violation.
     * @param existingPosition
     *         Where in the URI this placeholder name already appears.
     * @param duplicatePosition
     *         THe position where the offending placeholder appears.
     */
    public DuplicateUriPlaceholderException(String placeholderName, String existingPosition, String duplicatePosition) {

        this.placeholderName = placeholderName;
        this.existingPosition = existingPosition;
        this.duplicatePosition = duplicatePosition;
    }

    /**
     * Gets the position of the duplicated entry.
     *
     * @return The position of the duplicated entry.
     */
    public String getDuplicatePosition() {

        return duplicatePosition;
    }

    /**
     * Gets the position of the existing entry.
     *
     * @return The position of the existing entry.
     */
    public String getExistingPosition() {

        return existingPosition;
    }

    /**
     * Gets the name of the placeholder in violation.
     *
     * @return The name of the placeholder in violation.
     */
    public String getPlaceholderName() {

        return placeholderName;
    }
}
