package com.nudroid.annotation.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
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
@SupportedAnnotationTypes({ "com.nudroid.annotation.provider.delegate.Authority",
        "com.nudroid.annotation.provider.delegate.ContentUri",
        "com.nudroid.annotation.provider.delegate.ContentValuesRef", "com.nudroid.annotation.provider.delegate.Delete",
        "com.nudroid.annotation.provider.delegate.Insert", "com.nudroid.annotation.provider.delegate.PathParam",
        "com.nudroid.annotation.provider.delegate.Projection", "com.nudroid.annotation.provider.delegate.Query",
        "com.nudroid.annotation.provider.delegate.QueryParam", "com.nudroid.annotation.provider.delegate.Selection",
        "com.nudroid.annotation.provider.delegate.SelectionArgs", "com.nudroid.annotation.provider.delegate.SortOrder",
        "com.nudroid.annotation.provider.delegate.Update" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({ "persistence.annotation.log.level" })
public class PersistenceAnnotationProcessor extends AbstractProcessor {

    private LoggingUtils logger;

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private boolean initialized = false;
    private int iterationRun = 0;

    private Continuation continuation;
    private Metadata metadata;

    private AuthorityProcessor authorityProcessor;
    private QueryProcessor queryProcessor;

    private SourceCodeGenerator sourceCodeGenerator;

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

        logger = new LoggingUtils(env.getMessager(), logLevel);

        logger.debug("Initializing nudroid persistence annotation processor.");

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();

        continuation = new Continuation(filer, elementUtils, logger);
        metadata = new Metadata(elementUtils, typeUtils, logger);

        try {
            continuation.loadContinuation();
            initialized = true;
            logger.debug("Initialization complete.");
        } catch (Exception e) {
            logger.error("Unable to load continuation index file. Aborting annotation processor: " + e.getMessage());
            throw new AnnotationProcessorError(e);
        }

        final ProcessorContext processorContext = new ProcessorContext(continuation, metadata, processingEnv,
                elementUtils, typeUtils, logger);
        authorityProcessor = new AuthorityProcessor(processorContext);
        queryProcessor = new QueryProcessor(processorContext);
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

        logger.info("Start processing " + roundEnv.getRootElements());

        iterationRun++;

        if (!initialized) {

            logger.error("Annotation processor not initialized. Aborting.");

            return false;
        }

        if (isFirstRun()) {

            Set<Element> elementsToProcess = continuation.getElementsToProcess(roundEnv);

            for (Element rootClass : elementsToProcess) {

                if (ElementUtils.isClassOrInterface(rootClass)) {

                    authorityProcessor.processAuthorityOnClass((TypeElement) rootClass);
                    queryProcessor.processQueriesOnClass((TypeElement) rootClass);
                }
            }

            sourceCodeGenerator.generateCompanionSourceCode(metadata);
            continuation.saveContinuation();
        }

        return true;
    }

    private boolean isFirstRun() {

        return iterationRun == 1;
    }
}
