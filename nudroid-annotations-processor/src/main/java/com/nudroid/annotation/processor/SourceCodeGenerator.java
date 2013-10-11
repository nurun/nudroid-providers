package com.nudroid.annotation.processor;

import java.io.Writer;
import java.util.Properties;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * Generates the source code for the content provider delegates based on the gathered metadata.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class SourceCodeGenerator {

    private ProcessingEnvironment processingEnv;
    private LoggingUtils logger;
    private Filer filer;

    static final String GENERATED_CODE_BASE_PACKAGE = "com.nudroid.persistence";
    private static final String CONTENT_URI_REGISTRY_CLASS_NAME = "ContentUriRegistry";
    private static final String CONTENT_URI_REGISTRY_TEMPLATE_LOCATION = "com/nudroid/persistence/annotation/processor/ContentUriRegistryTemplate.vm";
    private static final String CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION = "com/nudroid/persistence/annotation/processor/RouterTemplate.vm";

    /**
     * Creates an instance of this class.
     * 
     * @param processorContext
     *            The processor context parameter object.
     */
    SourceCodeGenerator(ProcessorContext processorContext) {

        this.processingEnv = processorContext.processingEnv;
        this.logger = processorContext.logger;
        this.filer = processingEnv.getFiler();
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
    void generateCompanionSourceCode(Continuation continuation, Metadata metadata) {

        generateContentUriRegistrySourceCode(continuation, metadata);
        generateContentProviderRouterSourceCode(metadata);
    }

    private void generateContentUriRegistrySourceCode(Continuation continuation, Metadata metadata) {

        logger.debug(String.format("Generating ContentUriRegistry source code at %s.%s", GENERATED_CODE_BASE_PACKAGE,
                CONTENT_URI_REGISTRY_CLASS_NAME));

        Properties p = generateVelocityConfigurationProperties();
        Velocity.init(p);
        logger.debug(String.format("Configured velocity properties with %s.", p));

        VelocityContext context = new VelocityContext();

//        context.put("delegateClasses", metadata.getDelegateClasses().values());
//        context.put("contentProviderUris", metadata.getUniqueUris());
        context.put("continuationElements", continuation.getContinuationElements());
        logger.debug(String.format("Configured velocity context."));

        Template template = null;

        try {
            template = Velocity.getTemplate(CONTENT_URI_REGISTRY_TEMPLATE_LOCATION);
            logger.debug(String.format("Successfully loaded velocity template %s.",
                    CONTENT_URI_REGISTRY_TEMPLATE_LOCATION));

            JavaFileObject jfoContentUriRegistry = filer.createSourceFile(String.format("%s.%s",
                    GENERATED_CODE_BASE_PACKAGE, CONTENT_URI_REGISTRY_CLASS_NAME));
            logger.debug(String.format("Successfully created JavaFileObject for %s.%s.", GENERATED_CODE_BASE_PACKAGE,
                    CONTENT_URI_REGISTRY_CLASS_NAME));

            Writer writerContentUriRegistry = jfoContentUriRegistry.openWriter();
            logger.debug(String.format("Successfully openned JavaFileObject Writer."));

            template.merge(context, writerContentUriRegistry);
            logger.debug(String.format("Successfully merged velocity template."));

            writerContentUriRegistry.close();
            logger.debug(String.format("Successfully closed writer."));

            logger.debug(String.format("Source code generation for %s.%s is done.", GENERATED_CODE_BASE_PACKAGE,
                    CONTENT_URI_REGISTRY_CLASS_NAME));
        } catch (Exception e) {
            logger.error(String.format("Error processing velocity script '%s'", CONTENT_URI_REGISTRY_TEMPLATE_LOCATION));
            throw new AnnotationProcessorError(e);
        }
    }

    private void generateContentProviderRouterSourceCode(Metadata metadata) {

//        for (DelegateClass delegateClass : metadata.getDelegateClasses().values()) {
//            Properties p = generateVelocityConfigurationProperties();
//            Velocity.init(p);
//            VelocityContext context = new VelocityContext();
//            context.put("delegateClass", delegateClass);
//
//            Template template = null;
//
//            try {
//                template = Velocity.getTemplate(CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION);
//                JavaFileObject jfoContentUriRegistry = filer.createSourceFile(String.format("%s.%s",
//                        GENERATED_CODE_BASE_PACKAGE, delegateClass.getRouterSimpleName()));
//                Writer writerContentUriRegistry = jfoContentUriRegistry.openWriter();
//
//                template.merge(context, writerContentUriRegistry);
//                writerContentUriRegistry.close();
//            } catch (Exception e) {
//                logger.error(String.format("Error processing velocity script '%s'",
//                        CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION));
//                throw new AnnotationProcessorError(e);
//            }
//        }
    }

    private Properties generateVelocityConfigurationProperties() {

        Properties p = new Properties();
        p.put("resource.loader", "classpath");
        p.put("classpath.resource.loader.description", "Velocity Classpath Resource Loader");
        p.put("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return p;
    }
}
