package com.nudroid.annotation.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Manages annotation processing on incremental compilation in modern IDEs.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Continuation {

    static final String CONTENT_PROVIDER_DELEGATE_INDEX_FILE_NAME = "contentProviderDelegate.index";

    private HashSet<TypeElement> continuationElements = new HashSet<TypeElement>();
    private LoggingUtils logger;
    private Elements elementUtils;

    private Set<Element> classesToProcess;

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

        final TypeElement uriRegistryTypeElement = elementUtils
                .getTypeElement("com.nudroid.persistence.ContentUriRegistry");

        if (uriRegistryTypeElement == null) {

            logger.debug("com.nudroid.persistence.ContentUriRegistry not found. Skipping continuation.");
            return;
        }

        List<? extends AnnotationMirror> annotationMirrors = uriRegistryTypeElement.getAnnotationMirrors();

        AnnotationMirror continuationAnnotation = annotationMirrors.get(0);

        Map<? extends ExecutableElement, ? extends AnnotationValue> continuationValues = continuationAnnotation
                .getElementValues();
        Collection<?> continuationElements = null;
        for (ExecutableElement key : continuationValues.keySet()) {
            logger.debug("Loking for key " + key.getSimpleName());

            if (key.getSimpleName().toString().equals("value")) {
                logger.debug("Found value " + key.getSimpleName());

                AnnotationValue values = continuationValues.get(key);
                continuationElements = (Collection<?>) values.getValue();
            }
        }

        if (continuationElements == null) {

            logger.debug("No continuation elements found.");
            return;
        }

        for (Object continuationElement : continuationElements) {

            logger.debug(String.format("Attempting to load %s.", continuationElement));

            final TypeElement typeElement = elementUtils.getTypeElement(continuationElement.toString());

            if (typeElement != null) {

                this.continuationElements.add(typeElement);
            } else {

                logger.debug(String.format("Failed to load element %s.", continuationElement));
            }
        }

        if (this.continuationElements.isEmpty()) {

            logger.debug("No continuation elements found.");
        }

        logger.debug("Done loading continuation.");
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
    Set<Element> getElementsToProcess() {

        return Collections.unmodifiableSet(classesToProcess);
    }

    /**
     * @param annotationMirror
     * @return
     */
    List<Element> getElementsAnnotatedWith(AnnotationMirror annotationMirror) {

        List<Element> interceptorElements = new ArrayList<Element>();

        for (Element clazz : classesToProcess) {

            List<? extends AnnotationMirror> mirrors = clazz.getAnnotationMirrors();

            for (AnnotationMirror m : mirrors) {

                if (m.getAnnotationType().equals(annotationMirror.getAnnotationType())) {
                    interceptorElements.add(clazz);
                }
            }
        }

        return interceptorElements;
    }

    /**
     * @param annotationMirror
     * @return
     */
    List<Element> getElementsAnnotatedWith(TypeElement element) {

        List<Element> interceptorElements = new ArrayList<Element>();

        for (Element clazz : classesToProcess) {

            List<? extends AnnotationMirror> mirrors = clazz.getAnnotationMirrors();

            for (AnnotationMirror m : mirrors) {

                if (m.getAnnotationType().equals(element)) {
                    interceptorElements.add(clazz);
                }
            }
        }

        return interceptorElements;
    }

    /**
     * @param roundEnv
     */
    void calculateElementsToProcess(RoundEnvironment roundEnv) {

        classesToProcess = new HashSet<Element>();
        classesToProcess.addAll(roundEnv.getRootElements());
        logger.debug(String.format("Root elements being porocessed this round: %s", classesToProcess));

        continuationElements.removeAll(classesToProcess);

        if (!continuationElements.isEmpty()) {

            logger.debug(String.format("Adding continuation elements from previous builds: %s", continuationElements));
            classesToProcess.addAll(continuationElements);
        }
    }

    Set<TypeElement> getContinuationElements() {

        return Collections.unmodifiableSet(continuationElements);
    }
}