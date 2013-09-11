package com.nudroid.persistence.annotation.processor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
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

    private HashSet<String> continuation = new HashSet<String>();

    private LoggingUtils logger;

    public Continuation(Filer filer, LoggingUtils logger) {
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
                continuation.add(delegateName);
                logger.debug(String.format("Loaded delegate %s.", delegateName));
            }
        } finally {

            if (fileScanner != null) {
                fileScanner.close();
            }
        }

        logger.debug("Done loading continuation.");
    }

    Set<? extends Element> loadDelegateElements(Elements elementUtils) {

        logger.debug("Loading continuation elements.");
        Set<Element> elements = new HashSet<Element>();

        for (String elementName : continuation) {

            logger.debug(String.format("Attempting to load %s.", elementName));
            Element element = elementUtils.getTypeElement(elementName);

            if (element != null) {

                elements.add(element);
            } else {

                logger.debug(String.format("Failed to load element %s.", elementName));
            }
        }

        if (elements.isEmpty()) {

            logger.debug("No continuation elements found.");
        }

        return elements;
    }

    void addContentProviderDelegate(Element element) {

        continuation.add(element.toString());
    }

    Set<String> getContentProviderDelegateNames() {

        return Collections.unmodifiableSet(continuation);
    }
}
