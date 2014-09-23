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

import com.nudroid.annotation.processor.UsedBy;
import com.nudroid.annotation.provider.delegate.ContentProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * A delegate class handles ContentResolver requests on behalf of a ContentProvider. Each delegate class maps to a
 * content provider and vice versa. The content provider matches a content URI with a particular method in the delegate
 * class forwards (delegates) the call to the target method.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateClass {

    private final String qualifiedName;
    private final TypeElement typeElement;
    private final String basePackageName;
    private final String contentProviderSimpleName;
    private final String routerSimpleName;
    private final Authority authority;
    private boolean hasImplementedDelegateInterface;
    private int matcherUriIdCount = 0;

    //TODO Check if there's a more performing way of doing this check.
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

    private final Map<String, MatcherUri> matcherUriRegistry = new HashMap<>();

    /**
     * Creates an instance of this class.
     *
     * @param authority
     *         The authority being handled by the delegate class.
     * @param typeElement
     *         The {@link TypeElement} for the delegate class as provided by the round environment.
     */
    public DelegateClass(String authority, TypeElement typeElement) {

        this.typeElement = typeElement;
        this.qualifiedName = typeElement.toString();
        this.authority = new Authority(authority);

        String contentProviderBaseName = typeElement.getSimpleName()
                .toString();
        contentProviderBaseName = contentProviderBaseName.replaceAll("(?i)Delegate", "")
                .replaceAll("(?i)ContentProvider", "");

        StringBuilder providerSimpleName = new StringBuilder(contentProviderBaseName);
        StringBuilder routerSimpleName = new StringBuilder(typeElement.getSimpleName()
                .toString());
        Element parentElement = typeElement.getEnclosingElement();

        /* If the enclosing element is not a package, the delegate class is an inner class. Suffix the name with the
        names of the parent classes. */
        while (parentElement != null && !parentElement.getKind()
                .equals(ElementKind.PACKAGE)) {

            providerSimpleName.insert(0, "$")
                    .insert(0, parentElement.getSimpleName());
            routerSimpleName.insert(0, "$")
                    .insert(0, parentElement.getSimpleName());
            parentElement = parentElement.getEnclosingElement();
        }

        providerSimpleName.append("ContentProvider_");
        routerSimpleName.append("Router_");

        if (parentElement != null && parentElement.getKind()
                .equals(ElementKind.PACKAGE)) {

            this.basePackageName = ((PackageElement) parentElement).getQualifiedName()
                    .toString();
        } else {

            this.basePackageName = "";
        }

        this.routerSimpleName = routerSimpleName.toString();
        this.contentProviderSimpleName = providerSimpleName.toString();
    }

    //    /**
    //     * Registers a @Query path and query string to be handled by this delegate class.
    //     *
    //     * @param pathAndQuery
    //     *         The path and query string to register.
    //     * @param placeholderTargetTypes
    //     *         The types of the parameters mapping to the placeholders, in the order they appear.
    //     *
    //     * @return A new UriToMethodBinding object binding the path and query string combination to the target method.
    //     *
    //     * @throws DuplicatePathException
    //     *         If the path and query string has already been associated with an existing @Query DelegateMethod.
    //     */
    //    public UriToMethodBinding registerPathForQuery(String pathAndQuery,
    //                                                   List<UriMatcherPathPatternType> placeholderTargetTypes) {
    //
    //        MatcherUri matcherUri = getMatcherUriFor(pathAndQuery, placeholderTargetTypes);
    //        return matcherUri.registerQueryUri(pathAndQuery);
    //    }

    /**
     * Registers a uri binding.
     *
     * @param uriToMethodBinding
     *         the binding to register
     */
    public void registerBindingForQuery(UriToMethodBinding uriToMethodBinding, Consumer<List<ValidationError>> error) {

        MatcherUri matcherUri = getMatcherUriFor(uriToMethodBinding.getPath());
        List<ValidationError> errors = new ArrayList<>();
        matcherUri.registerBindingForQuery(uriToMethodBinding, errors);

        if (errors.size() > 0) {

            error.accept(errors);
        }
    }

    /**
     * Sets if the delegate class implements the delegate interface.
     *
     * @param doesImplementDelegateInterface
     *         <tt>true</tt> if the delegate class implements the {@link ContentProvider} interface, <tt>false</tt>
     *         otherwise.
     */
    public void setImplementsDelegateInterface(boolean doesImplementDelegateInterface) {
        this.hasImplementedDelegateInterface = doesImplementDelegateInterface;
    }

    /**
     * Gets the {@link TypeElement} mapped by this class.
     *
     * @return The {@link TypeElement} mapped by this class.
     */
    public TypeElement getTypeElement() {
        return typeElement;
    }

    /**
     * Gets the fully qualified name of the delegate class.
     *
     * @return The fully qualified name of the delegate class.
     */
    public String getQualifiedName() {

        return qualifiedName;
    }

    /**
     * Gets the authority associated with this class.
     *
     * @return The authority associated with this class.
     */
    @UsedBy("ContentProviderTemplate.stg")
    public Authority getAuthority() {

        return authority;
    }

    /**
     * Gets the simple name of the router class which will be created to route calls to this delegate class (i.e.
     * without the package name).
     *
     * @return The simple name of the router class which will be created to route calls to this delegate class.
     */
    public String getRouterSimpleName() {

        return routerSimpleName;
    }

    /**
     * Gets the simple name of this delegate's content provider (i.e. without the package name).
     *
     * @return The simple name of this delegate's content provider.
     */
    public String getContentProviderSimpleName() {

        return this.contentProviderSimpleName;
    }

    /**
     * Gets the base package name for the generated class files.
     *
     * @return The base package name for the generated source files.
     */
    public String getBasePackageName() {

        return basePackageName;
    }

    /**
     * Checks if the delegate class implements the {@link ContentProvider} interface.
     *
     * @return <tt>true</tt> if the delegate class implements the {@link ContentProvider} interface, <tt>false</tt>
     * otherwise.
     */
    @UsedBy("ContentProviderTemplate.stg")
    public boolean getImplementsContentProviderDelegateInterface() {

        return hasImplementedDelegateInterface;
    }

    /**
     * Gets the URIs this delegate class handles.
     *
     * @return The {@link MatcherUri}s this delegate class handles.
     */
    @UsedBy({"ContentProviderTemplate.stg", "RouterTemplateQuery.stg", "RouterTemplateUpdate.stg"})
    public NavigableSet<MatcherUri> getMatcherUris() {

        return matcherUris;
    }

    /**
     * Checks if a {@link MatcherUri} has already been created for the provided path. If yes, returns that instance. If
     * not, a new instance is created, registered and returned.
     *
     * @param path
     *         the path to check
     *
     * @return the {@link MatcherUri} for the path
     */
    public MatcherUri getMatcherUriFor(String path) {

        MatcherUri newMatcherUri = matcherUriRegistry.get(path);

        if (newMatcherUri == null) {

            newMatcherUri = new MatcherUri(authority, path);
            newMatcherUri.setId(++matcherUriIdCount);
            matcherUris.add(newMatcherUri);
            matcherUriRegistry.put(path, newMatcherUri);
        }

        return newMatcherUri;
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
        result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
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
        DelegateClass other = (DelegateClass) obj;
        if (qualifiedName == null) {
            if (other.qualifiedName != null) return false;
        } else if (!qualifiedName.equals(other.qualifiedName)) return false;
        return true;
    }
}