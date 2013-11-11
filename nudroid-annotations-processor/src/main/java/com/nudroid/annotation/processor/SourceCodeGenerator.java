package com.nudroid.annotation.processor;

import java.io.Writer;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.nudroid.annotation.processor.model.InterceptorBlueprint;
import com.nudroid.annotation.processor.model.DelegateClass;

/**
 * Generates the source code for the content provider delegates based on the gathered metadata.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class SourceCodeGenerator {

    private ProcessingEnvironment mProcessingEnv;
    private LoggingUtils mLogger;
    private Filer mFiler;

    private static final String CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION = "com/nudroid/annotation/processor/RouterTemplate.vm";
    private static final String CONTENT_PROVIDER_TEMPLATE_LOCATION = "com/nudroid/annotation/processor/ContentProviderTemplate.vm";
    private static final String CONCRETE_ANNOTATION_TEMPLATE_LOCATION = "com/nudroid/annotation/processor/ConcreteAnnotationTemplate.vm";

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
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
     * @param continuation
     * 
     * @param metadata
     *            The metadata object containing the information gathered during the processor phase.
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

        Set<InterceptorBlueprint> concreteAnnotations = new HashSet<InterceptorBlueprint>(
                metadata.getInterceptorBlueprintsForRound());

        for (InterceptorBlueprint annotation : concreteAnnotations) {

            mLogger.trace(String.format("Generating concrete annotation class %s.",
                    annotation.getAnnotationQualifiedName()));
            generateConcreteAnnotationSourceCode(annotation);
            metadata.popInterceptorBlueprint(annotation);
            mLogger.trace(String.format("Done generating concrete annotation class %s.",
                    annotation.getAnnotationQualifiedName()));

        }
    }

    private void generateContentProviderSourceCode(DelegateClass delegateClass) {

        Properties p = generateVelocityConfigurationProperties();
        Velocity.init(p);
        VelocityContext context = new VelocityContext();
        context.put("delegateClass", delegateClass);
        context.put("newline", "\n");

        Template template = null;

        try {
            template = Velocity.getTemplate(CONTENT_PROVIDER_TEMPLATE_LOCATION);

            JavaFileObject javaFile = null;

            if (StringUtils.isEmpty(delegateClass.getBasePackageName())) {

                javaFile = mFiler.createSourceFile(delegateClass.getContentProviderSimpleName());
            } else {

                javaFile = mFiler.createSourceFile(String.format("%s.%s", delegateClass.getBasePackageName(),
                        delegateClass.getContentProviderSimpleName()));
            }

            Writer writerContentUriRegistry = javaFile.openWriter();

            template.merge(context, writerContentUriRegistry);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            mLogger.error(String.format("Error processing velocity script '%s': %s",
                    CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION, e));
        }

        mLogger.trace(String.format("    Generated Content Provider for class %s.", delegateClass.getTypeElement()));
    }

    // TODO Check if RouterTemplate.vm can be cleaned up to be easier to understand
    private void generateContentProviderRouterSourceCode(DelegateClass delegateClass) {

        Properties p = generateVelocityConfigurationProperties();
        Velocity.init(p);
        VelocityContext context = new VelocityContext();
        context.put("delegateClass", delegateClass);
        context.put("newline", "\n");

        Template template = null;

        try {
            template = Velocity.getTemplate(CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION);

            JavaFileObject javaFile = null;

            if (StringUtils.isEmpty(delegateClass.getBasePackageName())) {

                javaFile = mFiler.createSourceFile(delegateClass.getRouterSimpleName());
            } else {

                javaFile = mFiler.createSourceFile(String.format("%s.%s", delegateClass.getBasePackageName(),
                        delegateClass.getRouterSimpleName()));
            }

            Writer writerContentUriRegistry = javaFile.openWriter();

            template.merge(context, writerContentUriRegistry);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            mLogger.error(String.format("Error processing velocity script '%s': %s",
                    CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION, e));
        }

        mLogger.trace(String.format("    Generated Router for class %s.", delegateClass.getTypeElement()));
    }

    private void generateConcreteAnnotationSourceCode(InterceptorBlueprint annotation) {

        Properties p = generateVelocityConfigurationProperties();
        Velocity.init(p);
        VelocityContext context = new VelocityContext();
        context.put("annotation", annotation);
        context.put("newline", "\n");

        Template template = null;

        try {
            template = Velocity.getTemplate(CONCRETE_ANNOTATION_TEMPLATE_LOCATION);

            JavaFileObject javaFile = null;

            if (StringUtils.isEmpty(annotation.getConcretePackageName())) {

                javaFile = mFiler.createSourceFile(annotation.getConcreteClassName());
            } else {

                javaFile = mFiler.createSourceFile(String.format("%s.%s", annotation.getConcretePackageName(),
                        annotation.getConcreteClassName()));
            }

            Writer writerContentUriRegistry = javaFile.openWriter();

            template.merge(context, writerContentUriRegistry);
            writerContentUriRegistry.close();
        } catch (Exception e) {
            mLogger.error(String.format("Error processing velocity script '%s': %s",
                    CONCRETE_ANNOTATION_TEMPLATE_LOCATION, e));
        }

        mLogger.trace("    Generating concrete annotation " + annotation.getConcreteClassName());
    }

    private Properties generateVelocityConfigurationProperties() {

        Properties p = new Properties();
        p.put("resource.loader", "classpath");
        p.put("classpath.resource.loader.description", "Velocity Classpath Resource Loader");
        p.put("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return p;
    }
}
