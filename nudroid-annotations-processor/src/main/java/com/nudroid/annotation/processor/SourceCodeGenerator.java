package com.nudroid.annotation.processor;

import java.io.Writer;
import java.util.Properties;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.nudroid.annotation.processor.model.DelegateClass;

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
	void generateCompanionSourceCode(Metadata metadata) {

		generateContentProviderRouterSourceCode(metadata);
	}

	private void generateContentProviderRouterSourceCode(Metadata metadata) {

		for (DelegateClass delegateClass : metadata.getDelegateClasses()) {

			Properties p = generateVelocityConfigurationProperties();
			Velocity.init(p);
			VelocityContext context = new VelocityContext();
			context.put("delegateClass", delegateClass);

			Template template = null;

			try {
				template = Velocity.getTemplate(CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION);
				JavaFileObject jfoContentUriRegistry = filer.createSourceFile(String.format("%s.%s",
				        GENERATED_CODE_BASE_PACKAGE, delegateClass.getRouterSimpleName()));
				Writer writerContentUriRegistry = jfoContentUriRegistry.openWriter();

				template.merge(context, writerContentUriRegistry);
				writerContentUriRegistry.close();
			} catch (Exception e) {
				logger.error(String.format("Error processing velocity script '%s'",
				        CONTENT_PROVIDER_ROUTER_TEMPLATE_LOCATION));
				throw new AnnotationProcessorException(e);
			}
		}
	}

	private Properties generateVelocityConfigurationProperties() {

		Properties p = new Properties();
		p.put("resource.loader", "classpath");
		p.put("classpath.resource.loader.description", "Velocity Classpath Resource Loader");
		p.put("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		return p;
	}
}
