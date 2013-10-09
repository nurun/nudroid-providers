package com.nudroid.annotation.processor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Manages annotation processing on incremental compilation in modern IDEs.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Continuation {

    static final String CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME = "contentProviderDelegate.index";

    private Filer filer;
    private HashSet<TypeElement> continuationElements = new HashSet<TypeElement>();
    private LoggingUtils logger;
    private Elements elementUtils;

    /**
     * Creates an instance of a continuation.
     * 
     * @param filer
     *            A filer instance from a RoundEnvironment.
     * @param elementUtils
     *            A {@link Elements} instance from a {@link RoundEnvironment}.
     * @param logger
     *            The instance of the logging utils to use for logging.
     */
    Continuation(Filer filer, Elements elementUtils, LoggingUtils logger) {

        this.filer = filer;
        this.elementUtils = elementUtils;
        this.logger = logger;
    }

    /**
     * Loads the continuation information from previous compilation steps.
     * 
     * @throws IOException
     *             If unable to read the index files with the continuation information.
     */
    void loadContinuation() throws IOException {
        FileObject continuationDelegateIndexFile = filer.getResource(StandardLocation.SOURCE_OUTPUT,
                SourceCodeGenerator.GENERATED_CODE_BASE_PACKAGE, CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME);
        URI indexFileUri = continuationDelegateIndexFile.toUri();

        logger.debug("Obtained continuation index file path: " + indexFileUri);
        logger.debug("Attempting to load continuation index file.");

        File file = new File(indexFileUri);

        if (!file.exists()) {

            logger.debug(String.format("Continuation file not found. First compilation interation.", file));
            return;
        }

        logger.debug(String.format("Continuation file found. Loading continuation information.", file));

        Scanner fileScanner = null;

        try {

            fileScanner = new Scanner(file);

            while (fileScanner.hasNextLine()) {

                String delegateName = fileScanner.nextLine();

                logger.debug(String.format("Attempting to load %s.", delegateName));

                final TypeElement typeElement = elementUtils.getTypeElement(delegateName);

                if (typeElement != null) {

                    continuationElements.add(typeElement);
                } else {

                    logger.debug(String.format("Failed to load element %s.", delegateName));
                }
            }

            if (continuationElements.isEmpty()) {

                logger.debug("No continuation elements found.");
            }
        } finally {

            if (fileScanner != null) {
                fileScanner.close();
            }
        }

        logger.debug("Done loading continuation.");
    }

    /**
     * Saves the continuation information for future incremental builds.
     */
    void saveContinuation() {

        try {

            FileObject indexFile = filer.createResource(StandardLocation.SOURCE_OUTPUT,
                    SourceCodeGenerator.GENERATED_CODE_BASE_PACKAGE,
                    Continuation.CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME);
            Writer writer = indexFile.openWriter();
            PrintWriter printWriter = new PrintWriter(writer);

            for (Element indexedTypeName : continuationElements) {

                printWriter.println(indexedTypeName.toString());
            }

            printWriter.close();
            writer.close();
        } catch (Exception e) {

            logger.error(String.format("Error processing continuation index file '%s.%s'",
                    SourceCodeGenerator.GENERATED_CODE_BASE_PACKAGE,
                    Continuation.CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME));
            throw new AnnotationProcessorError(e);
        }
    }

    /**
     * Adds a {@link TypeElement} to this continuation.
     * <p/>
     * Elements added to this continuation can be retrieved by a subsequent incremental compilation.
     * 
     * @param element
     *            The element to be added.
     */
    void addContinuationElement(TypeElement element) {

        continuationElements.add(element);
    }

    /**
     * Calculates the final elements that should be processed by the round environment. Should only be called after a
     * call to {@link Continuation#loadContinuation()} is made.
     * 
     * @param roundEnv
     *            A reference to the round environment.
     * 
     * @return The set of elements to process. The resulting set will be root elements being processed by the round
     *         environment (i.e. {@link RoundEnvironment#getRootElements()}) plus any elements from this continuation.
     */
    Set<Element> getElementsToProcess(RoundEnvironment roundEnv) {

        Set<Element> classesToProcess = new HashSet<Element>();
        classesToProcess.addAll(roundEnv.getRootElements());
        logger.debug(String.format("Root elements being porocessed this round: %s", classesToProcess));

        continuationElements.removeAll(classesToProcess);

        if (!continuationElements.isEmpty()) {

            logger.debug(String.format("Adding continuation elements from previous builds: %s", continuationElements));
            classesToProcess.addAll(continuationElements);
        }

        return classesToProcess;
    }
}