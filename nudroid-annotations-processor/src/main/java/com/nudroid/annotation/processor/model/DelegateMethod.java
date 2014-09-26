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

import com.nudroid.annotation.processor.LoggingUtils;
import com.nudroid.annotation.processor.ProcessorUtils;
import com.nudroid.annotation.processor.UsedBy;
import com.nudroid.annotation.processor.ValidationErrorGatherer;
import com.nudroid.annotation.provider.delegate.ContentProvider;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Metadata for the delegate methods of a content provider delegate class.
 * <p>
 * Delegate methods are methods annotated with one of the delegate annotations (i.e {@link Query}, {@link Update},
 * {@link Insert}, {@link Delete} etc.
 */
public class DelegateMethod {

    private final String name;

    private String uriPath;
    private final List<Parameter> parameters = new ArrayList<>();
    private final Map<String, Parameter> pathParameters = new HashMap<>();
    private final List<String> queryStringParameterNames = new ArrayList<>();
    private final ExecutableElement executableElement;
    private final List<Interceptor> interceptorElements = new ArrayList<>();
    private List<Interceptor> inverseInterceptorElements = null;

    private DelegateMethod(ExecutableElement element) {

        this.name = element.getSimpleName()
                .toString();
        this.executableElement = element;
    }

    /**
     * Gets the query string parameters that should be injected when calling this method. These are the parameters
     * registered with the @QueryParam annotation.
     *
     * @return the list of query string parameters excepted
     */
    public List<String> getQueryStringParameterNames() {
        return queryStringParameterNames;
    }

    /**
     * Adds an interceptor point to this method. Interceptors work as an around advice around the delegate method.
     *
     * @param interceptor
     *         the interceptor to be applied on this method
     */
    public void addInterceptor(Interceptor interceptor) { this.interceptorElements.add(interceptor); }

    /**
     * Gets the {@link ExecutableElement} of the delegate method.
     *
     * @return the {@link ExecutableElement} of this method
     */
    public ExecutableElement getExecutableElement() { return executableElement; }

    /**
     * Gets the name of the method (i.e. method name without return type nor parameter).
     *
     * @return the method name
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public String getName() { return name; }

    /**
     * Gets the list of parameters this method accepts.
     *
     * @return the list of parameters
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public List<Parameter> getParameters() { return parameters; }

    /**
     * Gets the list of interceptors applied to this delegate method, in the order they are executed before the delegate
     * invocation.
     *
     * @return the list of interceptors
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public List<Interceptor> getBeforeInterceptorList() { return interceptorElements; }

    /**
     * Gets the path this method has been annotated with.
     *
     * @return the uri path
     */
    public String getUriPath() { return uriPath; }

    /**
     * Gets the list of interceptors applied to this delegate method, in the order they are executed after the delegate
     * invocation.
     *
     * @return the list of interceptors, in reverse order
     */
    @UsedBy({"RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public List<Interceptor> getAfterInterceptorList() {

        if (inverseInterceptorElements == null) {

            inverseInterceptorElements = new ArrayList<>(interceptorElements);
            Collections.reverse(interceptorElements);
        }

        return inverseInterceptorElements;
    }

    /**
     * Given a name, searches the parameter list for a parameter annotated with a @PathParam annotation matching the
     * given name.
     *
     * @param placeholderName
     *         the name to search for
     *
     * @return the parameter matching the criteria, or null if the parameter can't be found
     */
    Parameter findPathParameter(String placeholderName) { return pathParameters.get(placeholderName); }

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
                ", pathParameters=" + pathParameters +
                ", queryStringParameterNames=" + queryStringParameterNames +
                ", executableElement=" + executableElement +
                ", interceptorElements=" + interceptorElements +
                '}';
    }

    /**
     * Builder for a DelegateMethod.
     */
    public static class Builder implements ModelBuilder<DelegateMethod> {

        private static final String PATH_AND_QUERY_STRING_REGEXP = "[^\\?]*\\?.*";
        private final ExecutableElement executableElement;

        /**
         * Initializes the builder.
         *
         * @param annotatedMethod
         *         the Element for the method annotated with a delegate annotation
         */
        public Builder(ExecutableElement annotatedMethod) {

            this.executableElement = annotatedMethod;
        }

        /**
         * Creates the DelegateMethod instance.
         * <p>
         * {@inheritDoc}
         */
        public DelegateMethod build(ProcessorUtils processorUtils, Consumer<ValidationErrorGatherer> errorCallback) {

            ValidationErrorGatherer gatherer = new ValidationErrorGatherer();

            Query query = executableElement.getAnnotation(Query.class);
            String path = query.value();

            if (!validateMethodDeclaration(errorCallback, gatherer, path)) {

                return null;
            }

            DelegateMethod method = new DelegateMethod(this.executableElement);
            method.uriPath = path;

            List<? extends VariableElement> parameters = executableElement.getParameters();

            for (VariableElement methodParameter : parameters) {

                Parameter parameter =
                        new Parameter.Builder(methodParameter).build(processorUtils, gatherer::gatherErrors);
                method.parameters.add(parameter);

                if (parameter.isPathParameter()) {

                    method.pathParameters.put(parameter.getPlaceholderName(), parameter);
                } else if (parameter.isQueryParameter()) {

                    method.queryStringParameterNames.add(parameter.getPlaceholderName());
                }
            }

            gatherer.emmitCallbackIfApplicable(errorCallback);

            return method;
        }

        private boolean validateMethodDeclaration(Consumer<ValidationErrorGatherer> errorCallback,
                                                  ValidationErrorGatherer gatherer, String path) {
            TypeElement enclosingClass = (TypeElement) this.executableElement.getEnclosingElement();
            ContentProvider contentProviderDelegateAnnotation = enclosingClass.getAnnotation(ContentProvider.class);

            if (contentProviderDelegateAnnotation == null) {

                gatherer.gatherError(
                        String.format("Enclosing class must be annotated with @%s", ContentProvider.class.getName()),
                        this.executableElement, LoggingUtils.LogLevel.ERROR);

                return false;
            }

            if (path.matches(PATH_AND_QUERY_STRING_REGEXP)) {

                gatherer.gatherError("Query strings are not allowed in path expressions", executableElement,
                        LoggingUtils.LogLevel.ERROR);
                errorCallback.accept(gatherer);
                return false;
            }

            return true;
        }
    }
}
