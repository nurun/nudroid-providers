package com.nudroid.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * TODO: Add validations documented in the annotations package.<br/>
 * 
 * Annotation processor creating the bindings necessary for Android content provider delegates.
 * 
 * <h1>Logging</h1> This processor can be configured to display log messages during the annotation processing rounds.
 * Log messages are delivered through the processors {@link Messager} objects. In other words, they are issued as
 * compiler notes, warning or errors.
 * <p/>
 * The logging level can be configured through the property persistence.annotation.log.level. The logging level can
 * either be configured through a processor property (with the -A option) or a system property (with a -D option).
 * Processor property configuration takes precedence over the system property.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
// @f[off]
@SupportedAnnotationTypes({ "com.nudroid.annotation.provider.delegate.ContentProviderDelegate",
        "com.nudroid.annotation.provider.delegate.Delete", "com.nudroid.annotation.provider.delegate.Insert",
        "com.nudroid.annotation.provider.delegate.Query", "com.nudroid.annotation.provider.delegate.Update",

        "com.nudroid.annotation.provider.interceptor.ProviderInterceptorPoint" })
// @f[on]
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({ "persistence.annotation.log.level" })
public class ProviderAnnotationProcessor extends AbstractProcessor {

	private LoggingUtils mLogger;

	private Elements elementUtils;
	private Types typeUtils;
	private boolean initialized = false;

	private ContentProviderDelegateAnnotationProcessor contentProviderDelegateAnnotationProcessor;
	private QueryAnnotationProcessor queryAnnotationProcessor;

	private SourceCodeGenerator sourceCodeGenerator;

	private int mRound = 0;

	/**
	 * <p/>
	 * {@inheritDoc}
	 * 
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment env) {

		super.init(env);

		String logLevel = env.getOptions().get("persistence.annotation.log.level");

		if (logLevel == null) {
			logLevel = System.getProperty("persistence.annotation.log.level");
		}

		mLogger = new LoggingUtils(env.getMessager(), logLevel);

		mLogger.debug("Initializing nudroid persistence annotation processor.");

		elementUtils = env.getElementUtils();
		typeUtils = env.getTypeUtils();

		try {
			initialized = true;
			mLogger.debug("Initialization complete.");
		} catch (Exception e) {
			mLogger.error("Unable to load continuation index file. Aborting annotation processor: " + e.getMessage());
			throw new AnnotationProcessorException(e);
		}

		final ProcessorContext processorContext = new ProcessorContext(processingEnv, elementUtils, typeUtils, mLogger);
		contentProviderDelegateAnnotationProcessor = new ContentProviderDelegateAnnotationProcessor(processorContext);
		queryAnnotationProcessor = new QueryAnnotationProcessor(processorContext);
		sourceCodeGenerator = new SourceCodeGenerator(processorContext);
	}

	/**
	 * <p/>
	 * {@inheritDoc}
	 * 
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
	 *      javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		mLogger.info("Starting provider annotation processor round " + ++mRound);
		mLogger.trace("    Target classes " + roundEnv.getRootElements());

		if (!initialized) {

			mLogger.error("Annotation processor not initialized. Aborting.");

			return false;
		}

		Metadata metadata = new Metadata();
		contentProviderDelegateAnnotationProcessor.processContentProviderDelegateAnnotations(roundEnv, metadata);
		queryAnnotationProcessor.processQueryAnnotationOnMethods(roundEnv, metadata);

		sourceCodeGenerator.generateCompanionSourceCode(metadata);

		return true;
	}
}
