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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import com.google.common.base.Strings;
import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.provider.delegate.ContentProvider;
import com.nudroid.provider.delegate.ContentProviderDelegate;

/**
 * Processes the {@link ContentProvider} annotation on a {@link TypeElement}.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class ContentProviderProcessor {

    private final LoggingUtils mLogger;
    private final String mContentProviderDelegateInterfaceName;

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The context for the provider annotation processor.
     */
    ContentProviderProcessor(ProcessorContext processorContext) {

        this.mLogger = processorContext.logger;
        this.mContentProviderDelegateInterfaceName = ContentProviderDelegate.class.getName();
    }

    /**
     * Process the {@link ContentProvider} annotations on an annotation processor round.
     *
     * @param roundEnv
     *         the round environment to process
     * @param metadata
     *         the Metadata model to gather the results of the processing
     */
    void process(RoundEnvironment roundEnv, Metadata metadata) {

        mLogger.info(String.format("Start processing @%s annotations.", ContentProvider.class.getSimpleName()));

        Set<? extends Element> delegateClassTypes = roundEnv.getElementsAnnotatedWith(ContentProvider.class);

        if (delegateClassTypes.size() > 0) {

            String classesForTheRound = delegateClassTypes.stream()
                    .map(Element::toString)
                    .collect(Collectors.joining("\n        - "));

            mLogger.trace(String.format("    Classes annotated with @%s for the round:\n        - %s",
                    ContentProvider.class.getSimpleName(), classesForTheRound));
        }

        /*
         * Do not assume that because the @ContentProviderDelegate annotation can only be applied to types, only
         * TypeElements will be returned. Compilation errors on a class can let the compiler think the annotation is
         * applied to other elements even if it is correctly applied to a class, causing a class cast exception in
         * the forEach loop.
         */
        delegateClassTypes.stream()
                .filter(delegateClassType -> delegateClassType instanceof TypeElement)
                .forEach(delegateClassType -> {

                    mLogger.trace("    Processing " + delegateClassType);
                    processContentProviderDelegateAnnotation((TypeElement) delegateClassType, metadata);
                    mLogger.trace("    Done processing " + delegateClassType);
                });

        mLogger.info(String.format("Done processing @%s annotations.", ContentProvider.class.getSimpleName()));
    }

    private void processContentProviderDelegateAnnotation(TypeElement delegateClassType, Metadata metadata) {

        if (ElementUtils.isAbstract(delegateClassType)) {

            mLogger.trace("        Class is abstract. Signaling compilation error.");
            mLogger.error(String.format("@%s annotations are only allowed on concrete classes",
                    ContentProvider.class.getSimpleName()), delegateClassType);
        }

        if (!validateClassIsTopLevelOrStatic(delegateClassType)) {

            mLogger.trace("        Class is not top level nor static. Signaling compilation error.");
            mLogger.error(String.format("@%s annotations can only appear on top level or static classes",
                    ContentProvider.class.getSimpleName()), delegateClassType);
        }

        if (!validateClassHasPublicDefaultConstructor(delegateClassType)) {

            mLogger.trace("        Class does not have a public default constructor. Signaling compilation error.");
            mLogger.error(String.format("Classes annotated with @%s must have a public default constructor",
                    ContentProvider.class.getSimpleName()), delegateClassType);
        }

        ContentProvider contentProviderDelegateAnnotation = delegateClassType.getAnnotation(ContentProvider.class);

        if (contentProviderDelegateAnnotation == null) {

            return;
        }

        final String authorityName = contentProviderDelegateAnnotation.authority();
        mLogger.trace(String.format("        Authority name ='%s'.", authorityName));

        DelegateClass delegateClassForAuthority = metadata.getDelegateClassForAuthority(authorityName);

        if (delegateClassForAuthority != null) {

            mLogger.trace(
                    String.format("        Authority is already registered by class %s. Signaling compilation error.",
                            delegateClassForAuthority));
            mLogger.error(String.format("Authority '%s' has already been registered by class %s", authorityName,
                    delegateClassForAuthority.getQualifiedName()), delegateClassType);

            return;
        }

        mLogger.trace(
                String.format("        Added delegate class %s to authority '%s'.", delegateClassType, authorityName));

        delegateClassForAuthority = metadata.registerNewDelegateClass(authorityName, delegateClassType);

        if (!validateClassIsNotInDefaultPackage(delegateClassForAuthority)) {

            mLogger.trace("            Class is in the default package. Signaling compilation error.");
            mLogger.error(String.format("Content providers can not be created in the default package."),
                    delegateClassType);
        }

        // Eclipse issue: Can't use TypeMirror.equals as types will not match (even if they have the same qualified
        // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
        Set<String> interfaceNames = delegateClassType.getInterfaces()
                .stream()
                .map(TypeMirror::toString)
                .collect(Collectors.toSet());

        if (interfaceNames.contains(mContentProviderDelegateInterfaceName)) {

            mLogger.trace(String.format("            Class implements %s.", mContentProviderDelegateInterfaceName));
            delegateClassForAuthority.setImplementsDelegateInterface(true);
        } else {

            mLogger.trace(String.format("            Class does not implement %s.",
                    mContentProviderDelegateInterfaceName));
            delegateClassForAuthority.setImplementsDelegateInterface(false);
        }
    }

    private boolean validateClassIsTopLevelOrStatic(TypeElement delegateClassType) {

        Set<Modifier> delegateClassModifiers = delegateClassType.getModifiers();

        Element enclosingDelegateClassElement = delegateClassType.getEnclosingElement();

        return !(ElementUtils.isClassOrInterface(enclosingDelegateClassElement) &&
                !delegateClassModifiers.contains(Modifier.STATIC));

    }

    private boolean validateClassHasPublicDefaultConstructor(TypeElement delegateClassType) {

        List<? extends Element> enclosedElements = delegateClassType.getEnclosedElements();

        for (ExecutableElement constructor : ElementFilter.constructorsIn(enclosedElements)) {

            if (constructor.getParameters()
                    .size() == 0 && constructor.getModifiers()
                    .contains(Modifier.PUBLIC)) {

                return true;
            }
        }

        return false;
    }

    private boolean validateClassIsNotInDefaultPackage(DelegateClass delegateClass) {

        return !Strings.isNullOrEmpty(delegateClass.getBasePackageName());
    }
}
