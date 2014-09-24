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

import com.google.common.base.Strings;
import com.nudroid.annotation.processor.LoggingUtils;
import com.nudroid.annotation.processor.ProcessorUtils;
import com.nudroid.annotation.processor.ValidationErrorGatherer;
import com.nudroid.annotation.provider.delegate.ContentProvider;

import java.util.function.Consumer;

import javax.lang.model.element.TypeElement;

/**
 * A content provider authority, as denoted by the @ContentProvider annotation.
 */
public class Authority {

    private final String name;

    private Authority(String authorityName) {

        this.name = authorityName;
    }

    /**
     * Gets the authority name.
     *
     * @return the authority name
     */
    public String getName() {

        return name;
    }

    @Override
    public String toString() {
        return "Authority{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Authority authority = (Authority) o;

        return name.equals(authority.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Builder for Authority.
     */
    public static class Builder implements ModelBuilder<Authority> {

        private String authorityName;
        private final TypeElement typeElement;

        /**
         * Initializes the builder.
         *
         * @param providerAnnotation
         *         the @ContentProvider defining the authority
         * @param typeElement
         *         the element the @ContentProvider annotation is applied to
         */
        public Builder(ContentProvider providerAnnotation, TypeElement typeElement) {

            this.authorityName = providerAnnotation.authority();
            this.typeElement = typeElement;
        }

        /**
         * Builds an instance of the Authority class.
         * <p>
         * {@inheritDoc}
         */
        public Authority build(ProcessorUtils processorUtils, Consumer<ValidationErrorGatherer> errorCallback) {

            ValidationErrorGatherer gatherer = new ValidationErrorGatherer();

            if (Strings.isNullOrEmpty(this.authorityName)) {

                gatherer.gatherError("Authority name can't be empty.", this.typeElement, LoggingUtils.LogLevel.ERROR);
                errorCallback.accept(gatherer);
            }

            return new Authority(this.authorityName);
        }
    }
}
