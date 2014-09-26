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
import com.nudroid.annotation.processor.UsedBy;
import com.nudroid.annotation.processor.ValidationErrorGatherer;
import com.nudroid.annotation.provider.delegate.ContentProvider;
import com.nudroid.provider.delegate.ContentProviderDelegate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * Metadata for content provider delegate classes. A delegate class handles ContentResolver requests on behalf of a
 * ContentProvider. Each delegate class maps to a content provider and vice versa. The content provider matches a
 * content URI with a particular method in the delegate class and forwards (delegates) the call to the target method.
 */
public class DelegateClass {

    private String qualifiedName;
    private String packageName;
    private TypeElement typeElement;
    private String contentProviderSimpleName;
    private String routerSimpleName;
    private Authority authority;
    private boolean implementsDelegateInterface;
    private int matcherUriIdCount = 0;

    private final Map<String, MatcherUri> matcherUriRegistry = new HashMap<>();

    /* UriMatcher has an undocumented matching algorithm. It will match the first path segment (in the order they have
     * been added) and ignore the rest of the entries even if the selected match fails on a later stage. For example:
     *
     *     * / *
     *     page / * / *
     *
     * will not match 'page/section/33'. 'page' will match the first '*' so UriMatcher will try to use that entry to
     * validate the path. Since the path has three segments while the pattern only has 2, it will fail but it WILL NOT
     * try to match against the next (and correct) pattern.
     *
     * However, if we add these URIs in reverse order:
     *
     *     page / * / *
     *     * / *
     *
     * Then it will match since the first entry will be selected. Basically, it traverses the list of patterns and stops
     * as soon as it finds an entry whose first item match the input string.
     *
     * The sorting algorithm below sorts the entries by placing literal segments on top of the list based on where
     * they appear in the pth. The closer to the beginning of the path, the higher their priority. Paths with the
     * same priority are then sorted by path segment length.
     *
     * Uris will be sorted as they are added to this set by the provided closure.
     */
    private final NavigableSet<MatcherUri> matcherUris = new TreeSet<>((uri1, uri2) -> {

        String[] uri1Paths = uri1.getNormalizedPath()
                .split("/");
        String[] uri2Paths = uri2.getNormalizedPath()
                .split("/");

        /* First sort by path literals. */
        for (int i = 0; i < Math.min(uri1Paths.length, uri2Paths.length); i++) {

            if (!UriMatcherPathPatternType.isPattern(uri1Paths[i]) &&
                    UriMatcherPathPatternType.isPattern(uri2Paths[i])) {
                return -1;
            }

            if (UriMatcherPathPatternType.isPattern(uri1Paths[i]) &&
                    !UriMatcherPathPatternType.isPattern(uri2Paths[i])) {
                return 1;
            }
        }

        /* If uri structure is the same, sort by size. */
        int pathSegmentCountDelta = uri2Paths.length - uri1Paths.length;

        /* If size is the same, sort by alphabetical order. */
        if (pathSegmentCountDelta == 0) {

            for (int i = 0; i < uri1Paths.length; i++) {

                int order = uri1Paths[i].compareTo(uri2Paths[i]);

                if (order != 0) {
                    return order;
                }
            }
        }

        return pathSegmentCountDelta;
    });

    private DelegateClass() {

    }

    /**
     * Gets the {@link TypeElement} mapped by this class.
     *
     * @return the {@link TypeElement} mapped by this class
     */
    public TypeElement getTypeElement() {
        return typeElement;
    }

    /**
     * Gets the fully qualified name of the delegate class.
     *
     * @return the fully qualified name of the delegate class
     */
    public String getQualifiedName() { return qualifiedName; }

    /**
     * Gets the name of the package.
     *
     * @return the package name
     */
    public String getPackageName() { return packageName; }

    /**
     * Gets the content provider authority handled by this class.
     *
     * @return the authority associated with this class
     */
    @UsedBy("ContentProviderTemplate.stg")
    public Authority getAuthority() { return authority; }

    /**
     * Gets the simple name of the router class which will be created to route calls to this delegate class (i.e.
     * without the package name).
     *
     * @return the simple name of the router class which will be created to route calls to this delegate class
     */
    public String getRouterSimpleName() { return routerSimpleName; }

    /**
     * Gets the simple name of the content provider delegating calls to this class (i.e. without the package name).
     *
     * @return the simple name of content provider.
     */
    public String getContentProviderSimpleName() { return this.contentProviderSimpleName; }

    /**
     * Checks if this delegate class implements the {@link ContentProvider} interface.
     *
     * @return <tt>true</tt> if it does, <tt>false</tt> otherwise.
     */
    @UsedBy("ContentProviderTemplate.stg")
    public boolean getImplementsContentProviderDelegateInterface() { return implementsDelegateInterface; }

    /**
     * Gets the URIs this delegate class is responsible for handling. URIs are sorted to avoid conflicts when
     * registering them with UriMatcher.
     *
     * @return the {@link MatcherUri}s this delegate class handles
     */
    @UsedBy({"ContentProviderTemplate.stg", "RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public NavigableSet<MatcherUri> getMatcherUris() { return matcherUris; }

    /**
     * Checks if a {@link MatcherUri} has already been created for the provided path. If yes, returns that instance. If
     * not, a new instance is created, registered and returned.
     *
     * @param path
     *         the path to check
     *
     * @return the {@link MatcherUri} for the path
     */
    public MatcherUri findMatcherUri(String path) {

        return matcherUriRegistry.get(path);
    }

    /**
     * Registers a path and corresponding MatcherUri on this delegate class.
     *
     * @param path
     *         the path the MatcherUri binds to
     * @param matcherUri
     *         the MatcherUri
     */
    public void registerMatcherUri(String path, MatcherUri matcherUri) {

        matcherUris.add(matcherUri);
        matcherUriRegistry.put(path, matcherUri);
    }

    @Override
    public String toString() {
        return "DelegateClass{" +
                "qualifiedName='" + qualifiedName + '\'' +
                ", delegateClassTypeElement=" + typeElement +
                ", contentProviderSimpleName='" + contentProviderSimpleName + '\'' +
                ", routerSimpleName='" + routerSimpleName + '\'' +
                ", authority=" + authority +
                ", implementsDelegateInterface=" + implementsDelegateInterface +
                ", matcherUriIdCount=" + matcherUriIdCount +
                ", matcherUris=" + matcherUris +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DelegateClass that = (DelegateClass) o;

        return qualifiedName.equals(that.qualifiedName);
    }

    @Override
    public int hashCode() {
        return qualifiedName.hashCode();
    }

    /**
     * Builder for this delegate class.
     */
    public static class Builder implements ModelBuilder<DelegateClass> {

        private ContentProvider contentProviderAnnotation;
        private TypeElement delegateClassTypeElement;

        /**
         * Initializes the builder.
         *
         * @param contentProviderAnnotation
         *         the @ContentProvider annotation applied on the target delegate class
         * @param delegateClassTypeElement
         *         the java model element of the class annotated with @ContentProvider
         */
        public Builder(ContentProvider contentProviderAnnotation, TypeElement delegateClassTypeElement) {

            this.contentProviderAnnotation = contentProviderAnnotation;
            this.delegateClassTypeElement = delegateClassTypeElement;
        }

        /**
         * Creates a new DelegateClass.
         * <p>
         * {@inheritDoc}
         */
        public DelegateClass build(ProcessorUtils processorUtils, Consumer<ValidationErrorGatherer> errorCallback) {

            ValidationErrorGatherer gatherer = new ValidationErrorGatherer();

            validateDelegateClassStructure(processorUtils, gatherer);

            PackageElement packageElement = processorUtils.findPackage(this.delegateClassTypeElement);

            if (Strings.isNullOrEmpty(packageElement.getQualifiedName()
                    .toString())) {
                gatherer.gatherError("Delegate classes cannot be defined in the default package.",
                        this.delegateClassTypeElement, LoggingUtils.LogLevel.ERROR);
            }

            String providerSimpleName = processorUtils.generateCompositeElementName(this.delegateClassTypeElement)
                    .replaceAll("(?i)Delegate", "")
                    .replaceAll("(?i)ContentProvider", "") + "ContentProvider_";

            String routerSimpleName =
                    processorUtils.generateCompositeElementName(this.delegateClassTypeElement) + "Router_";

            DelegateClass delegateClass = new DelegateClass();

            delegateClass.authority =
                    new Authority.Builder(this.contentProviderAnnotation, delegateClassTypeElement).build(
                            processorUtils, gatherer::gatherErrors);
            delegateClass.typeElement = this.delegateClassTypeElement;
            delegateClass.qualifiedName = this.delegateClassTypeElement.toString();
            delegateClass.packageName = packageElement.getQualifiedName()
                    .toString();

            if (processorUtils.implementsInterface(this.delegateClassTypeElement, ContentProviderDelegate.class)) {
                delegateClass.implementsDelegateInterface = true;
            }

            delegateClass.routerSimpleName = routerSimpleName;
            delegateClass.contentProviderSimpleName = providerSimpleName;

            gatherer.emmitCallbackIfApplicable(errorCallback);

            return delegateClass;
        }

        private void validateDelegateClassStructure(ProcessorUtils processorUtils, ValidationErrorGatherer gatherer) {

            checkIsNotAbstract(processorUtils, gatherer);
            checkIsTopLevelOrStatic(processorUtils, gatherer);
            checkHasDefaultConstructor(gatherer);
        }

        private void checkIsNotAbstract(ProcessorUtils processorUtils, ValidationErrorGatherer gatherer) {
            if (processorUtils.isAbstract(this.delegateClassTypeElement)) {

                gatherer.gatherError(String.format("@%s annotations are only allowed on concrete classes",
                                ContentProvider.class.getSimpleName()), this.delegateClassTypeElement,
                        LoggingUtils.LogLevel.ERROR);
            }
        }

        private void checkIsTopLevelOrStatic(ProcessorUtils processorUtils, ValidationErrorGatherer gatherer) {
            Set<Modifier> delegateClassModifiers = this.delegateClassTypeElement.getModifiers();

            Element enclosingDelegateClassElement = this.delegateClassTypeElement.getEnclosingElement();

            if (processorUtils.isClassOrInterface(enclosingDelegateClassElement) &&
                    !delegateClassModifiers.contains(Modifier.STATIC)) {

                gatherer.gatherError(String.format("@%s annotations can only appear on top level or static classes",
                        ContentProvider.class.getSimpleName()), delegateClassTypeElement, LoggingUtils.LogLevel.ERROR);
            }
        }

        private void checkHasDefaultConstructor(ValidationErrorGatherer gatherer) {
            List<? extends Element> enclosedElements = this.delegateClassTypeElement.getEnclosedElements();

            boolean hasDefaultConstructor = false;
            List<ExecutableElement> constructors = ElementFilter.constructorsIn(enclosedElements);

            for (int i = 0; i < constructors.size() && !hasDefaultConstructor; i++) {

                ExecutableElement constructor = constructors.get(i);

                if (constructor.getParameters()
                        .size() == 0 && constructor.getModifiers()
                        .contains(Modifier.PUBLIC)) {

                    hasDefaultConstructor = true;
                }
            }

            if (!hasDefaultConstructor) {

                gatherer.gatherError(String.format("Classes annotated with @%s must have a public default constructor",
                                ContentProvider.class.getSimpleName()), this.delegateClassTypeElement,
                        LoggingUtils.LogLevel.ERROR);
            }
        }
    }
}