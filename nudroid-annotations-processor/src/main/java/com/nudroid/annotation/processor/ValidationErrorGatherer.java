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

import com.nudroid.annotation.processor.model.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.Element;

/**
 * Gathers validation errors and creates a list of them for later processing.
 */
public class ValidationErrorGatherer {

    private List<ValidationError> gatheredErrors = new ArrayList<>();

    /**
     * Gathers a validation error.
     *
     * @param message
     *         the error message
     * @param offendingElement
     *         the offending element the message is attached to
     * @param severity
     *         the severity of the error
     *
     * @return itself, so calls can be chained
     */
    public ValidationErrorGatherer gatherError(String message, Element offendingElement,
                                               LoggingUtils.LogLevel severity) {

        ValidationError error = new ValidationError(message, offendingElement, severity);
        gatheredErrors.add(error);
        return this;
    }

    /**
     * Gathers errors from the provided gatherer, adding them to our own list.
     *
     * @param gatherer
     *         the gatherer to copy errors from
     */
    public ValidationErrorGatherer gatherErrors(ValidationErrorGatherer gatherer) {

        gatheredErrors.addAll(gatherer.getErrorListCopy());
        return this;
    }

    /**
     * Whether or not this gathered has logged any errors.
     *
     * @return <tt>true</tt> if it has, <tt>false</tt> otherwise
     */
    public boolean hasErrors() {
        return gatheredErrors.size() > 0;
    }

    /**
     * Gets an unmodifiable copy of the list of gathered errors. This method returns a new list instance so be conscious
     * on for loops.
     *
     * @return the list of errors gathered by this class
     */
    public List<ValidationError> getErrorListCopy() {

        return Collections.unmodifiableList(gatheredErrors);
    }

    /**
     * Emits the error messages gathered by this class to the provided logger.
     *
     * @param loggingUtils
     *         the logger to use
     */
    public void logErrors(LoggingUtils loggingUtils) {

        for (ValidationError error : gatheredErrors) {

            loggingUtils.log(error.getMessage(), error.getElement(), error.getSeverity());
        }
    }

    /**
     * Emits the error to the provided callback if errors have been detected.
     *
     * @param errorCallback
     *         the callback error to be notified of errors
     */
    public void emmitCallbackIfApplicable(Consumer<ValidationErrorGatherer> errorCallback) {

        if (gatheredErrors.size() > 0) {

            errorCallback.accept(this);
        }
    }
}
