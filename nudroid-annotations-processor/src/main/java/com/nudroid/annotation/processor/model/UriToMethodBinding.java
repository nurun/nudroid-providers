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

import com.google.common.base.Splitter;
import com.nudroid.annotation.processor.UsedBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A mapping between a path and a method. This class maps a path (+ query string) to a target delegate method.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class UriToMethodBinding {

    private static final String PLACEHOLDER_REGEXP = "\\{([^\\}]+)\\}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEXP);

    private DelegateMethod delegateMethod;
    private String path;
    private Map<String, PathParamBinding> pathParameterBindings = new HashMap<>();
    private Set<String> queryStringParameters = new HashSet<>();

    private UriToMethodBinding() {

    }

    /**
     * Gets the path this binding applies to. The path will already be normalized for a <a
     * href="http://developer.android.com/reference/android/content/UriMatcher.html">UriMatcher</a> (i.e. placeholder
     * names will be replaced by '*' or ''#).
     *
     * @return The normalized path for this URI.
     */
    public String getPath() {

        return path;
    }

    /**
     * Gets the delegate method for for this binding.
     *
     * @return the delegate method
     */
    public DelegateMethod getDelegateMethod() {

        return delegateMethod;
    }

    /**
     * Counts the parameters for the delegate method mapping to a query string parameter.
     *
     * @return the number of parameters mapping to a query string parameter
     */
    public int getQueryStringParameterCount() {

        return queryStringParameters.size();
    }

    /**
     * Checks if this delegate method has path parameters.
     *
     * @return <tt>true</tt> if yes, <tt>false</tt> otherwise
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public boolean getHasUriPlaceholders() {

        return pathParameterBindings.size() > 0;
    }

    /**
     * Gets the path parameter bindings.
     *
     * @return the path parameters bindings
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public Collection<PathParamBinding> getPathParameterBindings() {

        return pathParameterBindings.values();
    }

    /**
     * Gets the query parameter bindings.
     *
     * @return the query parameters bindings
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public Collection<String> getQueryStringParameterBindings() {

        return queryStringParameters;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UriToMethodBinding that = (UriToMethodBinding) o;

        if (!path.equals(that.path)) return false;
        return queryStringParameters.equals(that.queryStringParameters);
    }

    @Override
    public int hashCode() {

        int result = path.hashCode();
        result = 31 * result + queryStringParameters.hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "UriToMethodBinding{" +
                "delegateMethod=" + delegateMethod +
                ", path='" + path + '\'' +
                ", pathParameterBindings=" + pathParameterBindings +
                ", queryStringParameters=" + queryStringParameters +
                '}';
    }

    /**
     * Builder pattern.
     */
    public static class Builder {

        private String path;
        private DelegateMethod delegateMethod;

        /**
         * Sets the path it binds from. The path must contain the PathParam placeholders, if applicable.
         *
         * @param path
         *         the path to bind from
         *
         * @return this builder
         */
        public Builder path(String path) {

            this.path = path;
            return this;
        }

        /**
         * Sets the delegate method the path binds to.
         *
         * @param delegateMethod
         *         the delegate method the path binds to
         *
         * @return this builder
         */
        public Builder delegateMethod(DelegateMethod delegateMethod) {

            this.delegateMethod = delegateMethod;
            return this;
        }

        /**
         * Builds the binding between the path and delegate method
         *
         * @param errorValidationCallback
         *         the callback to be notified of validation errors
         *
         * @return the binding between the path and method
         */
        public UriToMethodBinding build(Consumer<List<ValidationError>> errorValidationCallback) {

            List<ValidationError> errorAccumulator = new ArrayList<>(delegateMethod.getParameters()
                    .size());

            UriToMethodBinding binding = new UriToMethodBinding();
            parsePlaceholders(binding, path, errorAccumulator);
            binding.delegateMethod = delegateMethod;
            binding.queryStringParameters = new HashSet<>(delegateMethod.getQueryStringParameterNames());

            if (errorAccumulator.size() > 0) {

                errorValidationCallback.accept(errorAccumulator);
            }

            return binding;
        }

        private void parsePlaceholders(UriToMethodBinding binding, String path,
                                       List<ValidationError> errorAccumulator) {

            Map<String, PathParamBinding> pathPlaceholders = new HashMap<>();
            String normalizedPath = path;

            /* ignores duplicated separators (i.e. "//")*/
            List<String> pathElements = Splitter.on('/')
                    .trimResults()
                    .omitEmptyStrings()
                    .splitToList(path);

            for (int i = 0; i < pathElements.size(); i++) {

                Matcher m = PLACEHOLDER_PATTERN.matcher(pathElements.get(i));

                if (m.find()) {

                    String placeholderName = m.group(1);

                    Parameter parameter = delegateMethod.findPathParameter(placeholderName);

                    if (parameter == null) {

                        ValidationError error = new ValidationError(
                                String.format("Placeholder '%s' is not mapped by any @PathParam parameters",
                                        placeholderName), delegateMethod.getExecutableElement(), null);
                        errorAccumulator.add(error);
                        continue;
                    }

                    PathParamBinding existingPlaceholder = pathPlaceholders.get(placeholderName);

                    if (existingPlaceholder != null) {
                        ValidationError error = new ValidationError(String.format(
                                "Placeholder '%s' appearing at position '%s' is already present at position '%s' in " +
                                        "'%s'", placeholderName, i, existingPlaceholder.getPosition(), path),
                                delegateMethod.getExecutableElement(), null);
                        errorAccumulator.add(error);
                        continue;
                    }

                    PathParamBinding placeholder = new PathParamBinding(placeholderName, i,
                            UriMatcherPathPatternType.fromClass(parameter.getParameterType()));
                    pathPlaceholders.put(placeholderName, placeholder);

                    normalizedPath = normalizedPath.replaceAll(PLACEHOLDER_REGEXP, placeholder.getPatternType()
                            .getPattern());
                }
            }

            binding.pathParameterBindings = pathPlaceholders;
            binding.path = normalizedPath;
        }
    }
}
