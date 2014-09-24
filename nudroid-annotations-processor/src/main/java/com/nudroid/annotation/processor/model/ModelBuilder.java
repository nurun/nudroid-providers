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

import com.nudroid.annotation.processor.ProcessorUtils;
import com.nudroid.annotation.processor.ValidationErrorGatherer;

import java.util.function.Consumer;

/**
 * Interface for all Builders for the model classes.
 *
 * @param <T>
 *         the type of the model class to build
 */
public interface ModelBuilder<T> {

    /**
     * Creates a new instance of the model class. Validations are performed at this time and any errors are reported in
     * the error callback. The callback will only be invoked if errors have been found. For most cases, errors should
     * not prevent the annotation processor from running since it can continue to process other elements and gather more
     * errors. This is the reason validation exceptions are gathered instead of thrown.
     * <p>
     * In the event it does not make sense to continue processing for a particular element and a stable instance of the
     * model class can' be created, this method may return null. Clients must check for this value before proceeding
     * with other elements.
     *
     * @param processorUtils
     *         an instance of the ProcessorUtils class, usually required for parsing the metadata from the Java model
     *         classes.
     * @param errorCallback
     *         the callback to be executed in case errors have been found
     *
     * @return a new instance of the model class, or <tt>null</tt> if one couldn't be created
     */
    T build(ProcessorUtils processorUtils, Consumer<ValidationErrorGatherer> errorCallback);
}
