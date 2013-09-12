package com.nudroid.persistence.annotation.processor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
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
 * Loads processor metadata for incremental compilation. TODO: Finish documentation.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Continuation {

    static final String CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME = "contentProviderDelegate.index";

    private Filer filer;
    private HashSet<TypeElement> continuationElements = new HashSet<TypeElement>();
    private LoggingUtils logger;
    private Elements elementUtils;

    Continuation(Filer filer, Elements elementUtils, LoggingUtils logger) {

        this.elementUtils = elementUtils;
        this.filer = filer;
        this.logger = logger;
    }

    /**
     * Loads the configuration from previous compilation steps.
     * 
     * @throws IOException
     *             If unable to read the index files with the continuation information.
     */
    void loadContinuation() throws IOException {
        FileObject continuationDelegateIndexFile = filer.getResource(StandardLocation.SOURCE_OUTPUT,
                PersistenceAnnotationProcessor.GENERATED_CODE_BASE_PACKAGE, CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME);
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

    void saveContinuation() {
    
        try {
            FileObject indexFile = filer.createResource(StandardLocation.SOURCE_OUTPUT,
                    PersistenceAnnotationProcessor.GENERATED_CODE_BASE_PACKAGE,
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
                    PersistenceAnnotationProcessor.GENERATED_CODE_BASE_PACKAGE,
                    Continuation.CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME));
            throw new AnnotationProcessorError(e);
        }
    }

    Set<TypeElement> getContinuationElements() {
        
        return Collections.unmodifiableSet(continuationElements);
    }

    void addContinuationElement(TypeElement element) {

        continuationElements.add(element);
    }
    
    Set<Element> getElementsToProcess(RoundEnvironment roundEnv) {
        
        Set<Element> classesToProcess = new HashSet<Element>();
        classesToProcess.addAll(roundEnv.getRootElements());
        logger.debug(String.format("Root elements being porocessed this round: %s", classesToProcess));

        continuationElements.removeAll(classesToProcess);

        if (!continuationElements.isEmpty()) {

            logger.debug(String.format("Adding continuation elements from previous builds: %s",
                    continuationElements));
            classesToProcess.addAll(continuationElements);
        }

        return classesToProcess;
    }
}
