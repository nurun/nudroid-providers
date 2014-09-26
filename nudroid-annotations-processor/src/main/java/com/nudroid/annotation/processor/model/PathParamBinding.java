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
import com.nudroid.annotation.processor.UsedBy;
import com.nudroid.annotation.processor.ValidationErrorGatherer;

import java.util.function.Consumer;

/**
 * A placeholder in a delegate method annotation URI.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class PathParamBinding {

    private final int position;
    private final String name;
    private final UriMatcherPathPatternType patternType;

    private PathParamBinding(String name, int position, UriMatcherPathPatternType patternType) {

        this.name = name;
        this.position = position;
        this.patternType = patternType;
    }

    /**
     * Gets the position of this placeholder in the URI path.
     *
     * @return the placeholder's position in the URI path
     */
    public int getPosition() {

        return position;
    }

    /**
     * Gets the name of this placeholder in the URI path.
     *
     * @return the name of the placeholder
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getName() {

        return name;
    }

    /**
     * Gets the pattern type of this placeholder.
     *
     * @return the pattern type
     */
    UriMatcherPathPatternType getPatternType() {
        return patternType;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PathParamBinding other = (PathParamBinding) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PathPlaceholder{" +
                "position='" + position + '\'' +
                ", name='" + name + '\'' +
                ", patternType=" + patternType +
                '}';
    }

    /**
     * Builder for PathParamBinding.
     */
    public static class Builder implements ModelBuilder<PathParamBinding> {

        private final int position;
        private final String name;
        private final UriMatcherPathPatternType patternType;

        /**
         * Initializes the builder.
         *
         * @param name
         *         the name of the placeholder
         * @param position
         *         the position in the URI path
         * @param patternType
         *         the pattern type ('*' or '#')
         */
        public Builder(String name, int position, UriMatcherPathPatternType patternType) {

            this.name = name;
            this.position = position;
            this.patternType = patternType;
        }

        /**
         * Builds a new PathParamBinding.
         * <p>
         * {@inheritDoc}
         */
        public PathParamBinding build(ProcessorUtils processorUtils, Consumer<ValidationErrorGatherer> errorCallback) {

            return new PathParamBinding(this.name, this.position, this.patternType);
        }
    }
}
