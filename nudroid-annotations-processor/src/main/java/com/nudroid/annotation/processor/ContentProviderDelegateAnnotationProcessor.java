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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.provider.delegate.ContentProvider;

/**
 * Processes the {@link ContentProvider} annotation on a {@link TypeElement}.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class ContentProviderDelegateAnnotationProcessor {

    private LoggingUtils mLogger;
    private String mDelegateTypeName;

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The context for the provider annotation processor.
     */
    ContentProviderDelegateAnnotationProcessor(ProcessorContext processorContext) {

        this.mLogger = processorContext.logger;
        this.mDelegateTypeName = com.nudroid.provider.delegate.ContentProviderDelegate.class.getName()
                .toString();
    }

    /**
     * Process the {@link ContentProvider} annotations on this round.
     *
     * @param continuation
     *         The continuation environment.
     * @param roundEnv
     *         The round environment to process.
     * @param metadata
     *         The annotation metadata for the processor.
     */
    void process(Continuation continuation, RoundEnvironment roundEnv, Metadata metadata) {

        mLogger.info(String.format("Start processing @%s annotations.", ContentProvider.class.getSimpleName()));

        Set<? extends Element> delegateClassTypes =
                continuation.getElementsAnotatedWith(ContentProvider.class, roundEnv);

        if (delegateClassTypes.size() > 0) {
            mLogger.trace(String.format("    Classes annotated with @%s for the round:\n        - %s",
                    ContentProvider.class.getSimpleName(), Joiner.on("\n        - ")
                    .skipNulls()
                    .join(delegateClassTypes)));
        }

        for (Element delegateClassType : delegateClassTypes) {

            /*
             * Do not assume that because the @ContentProviderDelegate annotation can only be applied to types, only
             * TypeElements will be returned. Compilation errors on a class can let the compiler think the annotation is
             * applied to other elements even if it is correctly applied to a class, causing a class cast exception in
             * the for loop below.
             */
            if (delegateClassType instanceof TypeElement) {

                mLogger.trace("    Processing " + delegateClassType);
                processContentProviderDelegateAnnotation((TypeElement) delegateClassType, metadata);
                mLogger.trace("    Done processing " + delegateClassType);
            }
        }

        mLogger.info(String.format("Done processing @%s annotations.", ContentProvider.class.getSimpleName()));
    }

    private void processContentProviderDelegateAnnotation(TypeElement delegateClassType, Metadata metadata) {

        ContentProvider contentProviderDelegateAnnotation = delegateClassType.getAnnotation(ContentProvider.class);

        if (ElementUtils.isAbstract(delegateClassType)) {

            mLogger.trace("        Class is abstract. Signaling compilatoin error.");
            mLogger.error(String.format("@%s annotations are only allowed on concrete classes",
                            ContentProvider.class.getSimpleName()), delegateClassType);
        }

        if (!validateClassIsTopLevelOrStatic(delegateClassType)) {

            mLogger.trace("        Class is not top level nor static. Signaling compilatoin error.");
            mLogger.error(String.format("@%s annotations can only appear on top level or static classes",
                    ContentProvider.class.getSimpleName()), delegateClassType);
        }

        if (!validateClassHasPublicDefaultConstructor(delegateClassType)) {

            mLogger.trace("        Class does not have a public default constructor. Signaling compilatoin error.");
            mLogger.error(String.format("Classes annotated with @%s must have a public default constructor",
                    ContentProvider.class.getSimpleName()), delegateClassType);
        }

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
        }

        mLogger.trace(
                String.format("        Added delegate class %s to authority '%s'.", delegateClassType, authorityName));

        metadata.registerNewDelegateClass(authorityName, delegateClassType);

        delegateClassForAuthority = metadata.getDelegateClassForAuthority(authorityName);

        if (!validateClassIsNotInDefaultPackage(delegateClassForAuthority)) {

            mLogger.trace("            Class is in the default package. Signaling compilatoin error.");
            mLogger.error(String.format("Content providers can not be created in the default package." +
                    " Android will prefix the content provider name with the application name, making it unable to" +
                    " find the correct class at runtime."), delegateClassType);
        }

        // Eclipse issue: Can't use TypeMirror.equals as types will not match (even if they have the same qualified
        // name) when Eclipse is doing incremental builds. Use qualified name for comparison instead.
        Set<String> interfaceNames = new HashSet<String>();

        for (TypeMirror e : delegateClassType.getInterfaces()) {
            interfaceNames.add(e.toString());
        }

        if (interfaceNames.contains(mDelegateTypeName)) {

            mLogger.trace(String.format("            Class implements %s.", mDelegateTypeName));
            delegateClassForAuthority.setImplementsDelegateInterface(true);
        } else {

            mLogger.trace(String.format("            Class does not implement %s.", mDelegateTypeName));
            delegateClassForAuthority.setImplementsDelegateInterface(false);
        }
    }

    private boolean validateClassIsTopLevelOrStatic(TypeElement delegateClassType) {

        Set<Modifier> delegateClassModifiers = delegateClassType.getModifiers();

        Element enclosingDelegateClassElement = delegateClassType.getEnclosingElement();

        if (ElementUtils.isClassOrInterface(enclosingDelegateClassElement) &&
                !delegateClassModifiers.contains(Modifier.STATIC)) {

            return false;
        }

        return true;
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

        return Strings.isNullOrEmpty(delegateClass.getBasePackageName()) == false;
    }
}
