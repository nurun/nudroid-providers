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
import com.nudroid.annotation.provider.delegate.Delete;
import com.nudroid.annotation.provider.delegate.Insert;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * <p>Holds information about the delegate method for a content provider.</p> <p> <p>Delegate methods are methods
 * annotated with one of the delegate annotations: {@link Query}, {@link Update}, {@link Insert}, or {@link
 * Delete}.</p>
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateMethod {

    private final String name;
    private final List<Parameter> parameters = new ArrayList<>();
    private final Map<String, Parameter> pathParameters = new HashMap<>();
    private final List<String> queryStringParameterNames = new ArrayList<>();
    private final ExecutableElement executableElement;
    private final List<InterceptorPoint> interceptorElements = new ArrayList<>();
    private List<InterceptorPoint> inverseInterceptorElements = null;

    /**
     * Creates an instance of this class.
     *
     * @param element
     *         The {@link javax.lang.model.element.ExecutableElement} representing this delegate method.
     */
    private DelegateMethod(ExecutableElement element) {

        this.name = element.getSimpleName()
                .toString();
        this.executableElement = element;
    }

    public List<String> getQueryStringParameterNames() {
        return queryStringParameterNames;
    }

    /**
     * Adds an interceptor point to this method. Interceptors work as an around advice around the delegate method.
     *
     * @param interceptor
     *         The interceptor type to add.
     */
    public void addInterceptor(InterceptorPoint interceptor) {

        this.interceptorElements.add(interceptor);
    }

    /**
     * Gets the {@link ExecutableElement} of the method represented by this class.
     *
     * @return The {@link ExecutableElement} of the method represented by this class.
     */
    public ExecutableElement getExecutableElement() {

        return executableElement;
    }

    /**
     * Gets the name of the method (i.e. method name without return type nor parameter).
     *
     * @return The method name.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getName() {

        return name;
    }

    /**
     * Gets the list of parameters this method accepts.
     *
     * @return List of parameters this method accepts.
     */
    public List<Parameter> getParameters() {

        return parameters;
    }

    /**
     * Gets the list of interceptors for this delegate method, in the order they are executed before the delegate
     * invocation.
     *
     * @return The list of interceptors for this delegate method.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public List<InterceptorPoint> getBeforeInterceptorList() {

        return interceptorElements;
    }

    /**
     * Gets the list of interceptors for this delegate method, in the order they are executed after the delegate
     * Invocation.
     *
     * @return The list of interceptors for this delegate method.
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public List<InterceptorPoint> getAfterInterceptorList() {

        if (inverseInterceptorElements == null) {

            inverseInterceptorElements = new ArrayList<>(interceptorElements);
            Collections.reverse(interceptorElements);
        }

        return inverseInterceptorElements;
    }

    /**
     * Given a name, searches the parameter list for a parameter annotated with PathParam matching the given name.
     *
     * @param placeholderName
     *         the name to search for
     *
     * @return The parameters matching the criteria, or null if the parameter can't be found
     */
    Parameter findPathParameter(String placeholderName) {

        return pathParameters.get(placeholderName);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DelegateMethod that = (DelegateMethod) o;

        if (!name.equals(that.name)) return false;
        if (!parameters.equals(that.parameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DelegateMethod{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters +
                ", pathParameters=" + pathParameters +
                ", queryStringParameterNames=" + queryStringParameterNames +
                ", executableElement=" + executableElement +
                ", interceptorElements=" + interceptorElements +
                ", inverseInterceptorElements=" + inverseInterceptorElements +
                '}';
    }

    /**
     * Builder pattern.
     */
    public static class Builder {

        private final ExecutableElement executableElement;

        /**
         * Sets the executable element this delegate method represents.
         *
         * @param queryMethod
         *         the method annotated with @Query
         */
        public Builder(ExecutableElement queryMethod) {

            this.executableElement = queryMethod;
        }

        public DelegateMethod build(ProcessorUtils processorUtils, Consumer<List<ValidationError>> error) {

            List<ValidationError> errorAccumulator = new ArrayList<>();
            DelegateMethod method = new DelegateMethod(this.executableElement);

            List<? extends VariableElement> parameters = executableElement.getParameters();

            for (VariableElement methodParameter : parameters) {

                Parameter parameter = new Parameter.Builder(methodParameter).build(processorUtils, errorAccumulator);
                method.parameters.add(parameter);

                if (parameter.isPathParameter()) {

                    method.pathParameters.put(parameter.getPlaceholderName(), parameter);
                } else if (parameter.isQueryParameter()) {

                    method.queryStringParameterNames.add(parameter.getPlaceholderName());
                }
            }

            if (errorAccumulator.size() > 0) {
                error.accept(errorAccumulator);
            }

            return method;
        }
    }
}
