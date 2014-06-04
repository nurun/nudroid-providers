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
 * Signal an internal error while processing the persistence annotations.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class AnnotationProcessorException extends RuntimeException {

    private static final long serialVersionUID = 379279303608925588L;

    /**
     * @see Exception#Exception()
     */
    public AnnotationProcessorException() {
        super();
    }

    /**
     * @see Exception#Exception(String)
     */
    public AnnotationProcessorException(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public AnnotationProcessorException(Throwable cause) {
        super(cause);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public AnnotationProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}