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

import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import com.google.common.base.Strings;
import com.nudroid.annotation.processor.model.DelegateClass;
import com.nudroid.annotation.processor.model.InterceptorAnnotationBlueprint;

/**
 * Generates the source code for the content provider delegates based on the gathered metadata.
 *
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class SourceCodeGenerator {

    private ProcessingEnvironment mProcessingEnv;
    private LoggingUtils mLogger;
    private Filer mFiler;

    private static final String CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION =
            "com/nudroid/annotation/processor/RouterTemplate.stg";
    private static final String CONTENT_PROVIDER_ROUTER_TEMPLATE_NAME = "RouterTemplate";
    private static final String CONTENT_PROVIDER_TEMPLATE_LOCATION =
            "com/nudroid/annotation/processor/ContentProviderTemplate.stg";
    private static final String CONTENT_PROVIDER_TEMPLATE_NAME = "ContentProviderTemplate";
    private static final String CONCRETE_ANNOTATION_TEMPLATE_LOCATION =
            "com/nudroid/annotation/processor/ConcreteAnnotationTemplate.stg";
    private static final String CONCRETE_ANNOTATION_TEMPLATE_NAME = "ConcreteAnnotationTemplate";

    /**
     * Creates an instance of this class.
     *
     * @param processorContext
     *         The processor context parameter object.
     */
    SourceCodeGenerator(ProcessorContext processorContext) {

        this.mProcessingEnv = processorContext.processingEnv;
        this.mLogger = processorContext.logger;
        this.mFiler = mProcessingEnv.getFiler();
    }

    /**
     * Generates the source code based on the gathered metadata. This method will generate the ContentUri registry and
     * the delegate classes.
     *
     * @param metadata
     *         The metadata object containing the information gathered during the processor phase.
     */
    void generateCompanionSourceCode(Metadata metadata) {

        Set<DelegateClass> delegateClasses = new HashSet<DelegateClass>(metadata.getDelegateClassesForRound());

        for (DelegateClass delegateClass : delegateClasses) {

            mLogger.trace("Generating source code for class " + delegateClass.getTypeElement());

            generateContentProviderSourceCode(delegateClass);
            generateContentProviderRouterSourceCode(delegateClass);
            metadata.popDelegateClass(delegateClass);

            mLogger.trace("Done generating source code for class " + delegateClass.getTypeElement());
        }

        Set<InterceptorAnnotationBlueprint> concreteAnnotations =
                new HashSet<InterceptorAnnotationBlueprint>(metadata.getInterceptorBlueprintsForRound());

        for (InterceptorAnnotationBlueprint annotation : concreteAnnotations) {

            mLogger.trace(
                    String.format("Generating concrete annotation class %s.", annotation.getAnnotationQualifiedName()));
            generateConcreteAnnotationSourceCode(annotation);
            metadata.popInterceptorBlueprint(annotation);
            mLogger.trace(String.format("Done generating concrete annotation class %s.",
                    annotation.getAnnotationQualifiedName()));

        }
    }

    private void generateContentProviderSourceCode(DelegateClass delegateClass) {

        // Properties p = generateVelocityConfigurationProperties();
        // Velocity.init(p);
        // VelocityContext context = new VelocityContext();
        // context.put("delegateClass", delegateClass);
        // context.put("newline", "\n");
        //
        // Template template = null;
        //
        // try {
        // template = Velocity.getTemplate(CONTENT_PROVIDER_TEMPLATE_LOCATION);
        //
        // JavaFileObject javaFile = null;
        //
        // if (StringUtils.isEmpty(delegateClass.getBasePackageName())) {
        //
        // javaFile = mFiler.createSourceFile(delegateClass.getContentProviderSimpleName());
        // } else {
        //
        // javaFile = mFiler.createSourceFile(String.format("%s.%s", delegateClass.getBasePackageName(),
        // delegateClass.getContentProviderSimpleName()));
        // }
        //
        // Writer writerContentUriRegistry = javaFile.openWriter();
        //
        // template.merge(context, writerContentUriRegistry);
        // writerContentUriRegistry.close();
        // } catch (Exception e) {
        // mLogger.error(String.format("Error processing velocity script '%s': %s",
        // CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION, e));
        // }
        //
        // mLogger.trace(String.format("    Generated Content Provider for class %s.", delegateClass.getTypeElement()));

        try {
            STGroupFile g = new STGroupFile(CONTENT_PROVIDER_TEMPLATE_LOCATION);
            ST st = g.getInstanceOf(CONTENT_PROVIDER_TEMPLATE_NAME);
            st.add("delegateClass", delegateClass);
            String result = st.render();

            JavaFileObject javaFile = null;

            if (Strings.isNullOrEmpty(delegateClass.getBasePackageName())) {

                javaFile = mFiler.createSourceFile(delegateClass.getContentProviderSimpleName());
            } else {

                javaFile = mFiler.createSourceFile(String.format("%s.%s", delegateClass.getBasePackageName(),
                        delegateClass.getContentProviderSimpleName()));
            }

            Writer writerContentUriRegistry = javaFile.openWriter();
            writerContentUriRegistry.write(result);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            mLogger.error(
                    String.format("Error processing velocity script '%s': %s", CONCRETE_ANNOTATION_TEMPLATE_LOCATION,
                            e));
        }

        mLogger.trace(String.format("    Generated Content Provider for class %s.", delegateClass.getTypeElement()));
    }

    private void generateContentProviderRouterSourceCode(DelegateClass delegateClass) {

        try {
            STGroupFile g = new STGroupFile(CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION);
            ST st = g.getInstanceOf(CONTENT_PROVIDER_ROUTER_TEMPLATE_NAME);
            st.add("delegateClass", delegateClass);
            String result = st.render();

            JavaFileObject javaFile = null;

            if (Strings.isNullOrEmpty(delegateClass.getBasePackageName())) {

                javaFile = mFiler.createSourceFile(delegateClass.getRouterSimpleName());
            } else {

                javaFile = mFiler.createSourceFile(String.format("%s.%s", delegateClass.getBasePackageName(),
                        delegateClass.getRouterSimpleName()));
            }

            Writer writerContentUriRegistry = javaFile.openWriter();
            writerContentUriRegistry.write(result);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            e.printStackTrace();
            mLogger.error(String.format("Error processing velocity script '%s': %s",
                    CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION, e));
        }

        mLogger.trace(String.format("    Generated Router for class %s.", delegateClass.getTypeElement()));
    }

    private void generateConcreteAnnotationSourceCode(InterceptorAnnotationBlueprint annotation) {

        try {
            STGroupFile g = new STGroupFile(CONCRETE_ANNOTATION_TEMPLATE_LOCATION);
            ST st = g.getInstanceOf(CONCRETE_ANNOTATION_TEMPLATE_NAME);
            st.add("annotation", annotation);
            String result = st.render();

            JavaFileObject javaFile = null;

            if (Strings.isNullOrEmpty(annotation.getConcretePackageName())) {

                javaFile = mFiler.createSourceFile(annotation.getConcreteClassName());
            } else {

                javaFile = mFiler.createSourceFile(
                        String.format("%s.%s", annotation.getConcretePackageName(), annotation.getConcreteClassName()));
            }

            Writer writerContentUriRegistry = javaFile.openWriter();
            writerContentUriRegistry.write(result);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            mLogger.error(
                    String.format("Error processing velocity script '%s': %s", CONCRETE_ANNOTATION_TEMPLATE_LOCATION,
                            e));
        }

        mLogger.trace("    Generated concrete annotation " + annotation.getConcreteClassName());
    }
}
