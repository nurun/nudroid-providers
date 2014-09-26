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

import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.InterceptorAnnotationBlueprints;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

/**
 * Generates the source code for the content provider delegates based on the gathered metadata.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class SourceCodeWriter {

    private final LoggingUtils logger;
    private final Filer filer;

    private static final String CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION =
            "com/nudroid/annotation/processor/RouterTemplate.stg";
    private static final String CONTENT_PROVIDER_ROUTER_TEMPLATE_NAME = "RouterTemplate";
    private static final String CONTENT_PROVIDER_TEMPLATE_LOCATION =
            "com/nudroid/annotation/processor/ContentProviderTemplate.stg";
    private static final String CONTENT_PROVIDER_TEMPLATE_NAME = "ContentProviderTemplate";
    private static final String CONCRETE_ANNOTATION_TEMPLATE_LOCATION =
            "com/nudroid/annotation/processor/ConcreteAnnotationTemplate.stg";
    private static final String CONCRETE_ANNOTATION_TEMPLATE_NAME = "ConcreteAnnotationTemplate";

    private static final String GENERATED_PACKAGE_NAME_SUFFIX = ".generated_";

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The processor context parameter object.
     */
    SourceCodeWriter(ProcessorContext processorContext) {

        ProcessingEnvironment mProcessingEnv = processorContext.processingEnv;
        this.logger = processorContext.logger;
        this.filer = mProcessingEnv.getFiler();
    }

    /**
     * Generates the source code based on the gathered metadata. This method will generate the ContentUri registry and
     * the delegate classes.
     *
     * @param metadata
     *         The metadata object containing the information gathered during the processor phase.
     */
    void generateCompanionSourceCode(Metadata metadata) {

        Set<DelegateClass> delegateClasses = new HashSet<>(metadata.getDelegateClassesForRound());

        for (DelegateClass delegateClass : delegateClasses) {

            logger.trace("Generating source code for class " + delegateClass.getTypeElement());

            generateContentProviderSourceCode(delegateClass);
            generateContentProviderRouterSourceCode(delegateClass);
            metadata.popDelegateClass(delegateClass);

            logger.trace("Done generating source code for class " + delegateClass.getTypeElement());
        }

        Set<InterceptorAnnotationBlueprints> concreteAnnotations =
                new HashSet<>(metadata.getInterceptorBlueprintsForRound());

        for (InterceptorAnnotationBlueprints annotation : concreteAnnotations) {

            logger.trace(
                    String.format("Generating concrete annotation class %s.", annotation.getAnnotationQualifiedName()));
            generateConcreteAnnotationSourceCode(annotation);
            metadata.popInterceptorBlueprint(annotation);
            logger.trace(String.format("Done generating concrete annotation class %s.",
                    annotation.getAnnotationQualifiedName()));

        }
    }

    //TODO Bug: If the delegate is named, say SampleContentProvider, the generated provider will have the same class
    // name in same package, causing compilation errors. Make sure the generated classes are unique.
    private void generateContentProviderSourceCode(DelegateClass delegateClass) {

        try {

            String packageName = delegateClass.getPackageName() + GENERATED_PACKAGE_NAME_SUFFIX;
            STGroupFile g = new STGroupFile(CONTENT_PROVIDER_TEMPLATE_LOCATION);
            ST st = g.getInstanceOf(CONTENT_PROVIDER_TEMPLATE_NAME);
            st.add("delegateClass", delegateClass);
            st.add("packageName", packageName);
            String result = st.render();

            JavaFileObject javaFile;

            javaFile = filer.createSourceFile(
                    String.format("%s.%s", packageName, delegateClass.getContentProviderSimpleName()));

            Writer writerContentUriRegistry = javaFile.openWriter();
            writerContentUriRegistry.write(result);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            logger.error(
                    String.format("Error processing velocity script '%s': %s", CONCRETE_ANNOTATION_TEMPLATE_LOCATION,
                            e));
        }

        logger.trace(String.format("    Generated Content Provider for class %s.", delegateClass.getTypeElement()));
    }

    private void generateContentProviderRouterSourceCode(DelegateClass delegateClass) {

        try {

            String packageName = delegateClass.getPackageName() + GENERATED_PACKAGE_NAME_SUFFIX;
            STGroupFile g = new STGroupFile(CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION);
            ST st = g.getInstanceOf(CONTENT_PROVIDER_ROUTER_TEMPLATE_NAME);
            st.add("delegateClass", delegateClass);
            st.add("packageName", packageName);
            String result = st.render();

            JavaFileObject javaFile;

            javaFile = filer.createSourceFile(String.format("%s.%s", packageName, delegateClass.getRouterSimpleName()));

            Writer writerContentUriRegistry = javaFile.openWriter();
            writerContentUriRegistry.write(result);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("Error processing velocity script '%s': %s",
                    CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION, e));
        }

        logger.trace(String.format("    Generated Router for class %s.", delegateClass.getTypeElement()));
    }

    private void generateConcreteAnnotationSourceCode(InterceptorAnnotationBlueprints annotation) {

        try {

            String packageName = annotation.getPackageName() + GENERATED_PACKAGE_NAME_SUFFIX;
            STGroupFile g = new STGroupFile(CONCRETE_ANNOTATION_TEMPLATE_LOCATION);
            ST st = g.getInstanceOf(CONCRETE_ANNOTATION_TEMPLATE_NAME);
            st.add("annotation", annotation);
            st.add("packageName", packageName);
            String result = st.render();

            JavaFileObject javaFile;

            javaFile = filer.createSourceFile(String.format("%s.%s", packageName, annotation.getConcreteClassSimpleName()));

            Writer writerContentUriRegistry = javaFile.openWriter();
            writerContentUriRegistry.write(result);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            logger.error(
                    String.format("Error processing velocity script '%s': %s", CONCRETE_ANNOTATION_TEMPLATE_LOCATION,
                            e));
        }

        logger.trace("    Generated concrete annotation " + annotation.getConcreteClassSimpleName());
    }
}
