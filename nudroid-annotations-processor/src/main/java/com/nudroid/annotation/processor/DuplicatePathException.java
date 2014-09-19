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

import javax.lang.model.element.ExecutableElement;

/**
 * Exception raised when the URI provided to the delegate method is duplicated.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DuplicatePathException extends IllegalUriPathException {

    private static final long serialVersionUID = -4364782083955709261L;
    private final ExecutableElement mOriginalMethod;

    /**
     * @param existingDelegateMethod
     *         The delegate method which already define the path.
     */
    public DuplicatePathException(ExecutableElement existingDelegateMethod) {
        super(String.format("An equivalent path has already been registerd by method %s", existingDelegateMethod));

        this.mOriginalMethod = existingDelegateMethod;
    }

    /**
     * Gets the method which originally registered the offending path.
     *
     * @return The method which originally registered the offending path.
     */
    public Object getOriginalMethod() {

        return mOriginalMethod;
    }
}
