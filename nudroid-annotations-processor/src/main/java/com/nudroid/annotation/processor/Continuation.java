package com.nudroid.annotation.processor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.nudroid.annotation.provider.interceptor.ProviderInterceptorPoint;

/**
 * Manages continuation of incremental compilation. On modern IDEs, compilation can be incremental (i.e. only the
 * modified classes are compiled on a round). Since the processor requires metadata extracted from other source files,
 * which might not be included in a particular compilation round on an IDE, not all information might be available to
 * Nudroid's annotation processor. This continuation utility class manages a store of processed elements from past
 * compilations.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Continuation {

    private static final String INTERCEPTOR_ANNOTTIONS_PROPERTY_KEY = "com.nudroid.annotation.processor.continuation.interceptor.annotations";
    private static final String INTERCEPTOR_CLASSES_PROPERTY_KEY = "com.nudroid.annotation.processor.continuation.interceptor.classes";
    private File mContinuationFile;
    private Set<TypeElement> mInterceptorAnnotationTypes = new HashSet<TypeElement>();
    private Set<TypeElement> mInterceptorClassTypes = new HashSet<TypeElement>();
    private Set<TypeElement> mInterceptorAnnotationTypesStack = new HashSet<TypeElement>();
    private LoggingUtils mLogger;
    private Elements mElementUtils;
    private Types mTypeUtils;

    /**
     * Creates a new continuation.
     * 
     * @param continuationFile
     *            The path for the continuation file.
     */
    Continuation(ProcessorContext processorContext, String continuationFile) {

        if (continuationFile != null) {
            this.mContinuationFile = new File(continuationFile);
        }

        this.mLogger = processorContext.logger;
        this.mElementUtils = processorContext.elementUtils;
        this.mTypeUtils = processorContext.typeUtils;
    }

    /**
     * Loads continuation information from the provided continuation file.
     */
    void loadContinuation() {

        mLogger.trace("Loading continuation file.");

        if (mContinuationFile == null) {

            mLogger.debug(String.format("Continuation file not ptovided. Skipping continuation.", mContinuationFile));
            return;
        }

        if (!mContinuationFile.exists()) {

            mLogger.debug(String
                    .format("Continuation file not found. First compilation interation.", mContinuationFile));
            return;
        }

        mLogger.debug(String.format("    Continuation file found at %s. Loading continuation information.",
                mContinuationFile.getPath()));

        Properties continuationProperties = new Properties();

        FileReader fileReader = null;

        try {
            fileReader = new FileReader(mContinuationFile);
            continuationProperties.load(fileReader);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Error while loading continuation file '%s'.",
                    mContinuationFile), e);
        } finally {
            FileUtils.close(fileReader);
        }

        Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();

        String interceptorAnnotations = continuationProperties.getProperty(INTERCEPTOR_ANNOTTIONS_PROPERTY_KEY);
        String interceptorClasses = continuationProperties.getProperty(INTERCEPTOR_CLASSES_PROPERTY_KEY);

        mLogger.trace(String.format("    Stored interceptor annotations: %s", interceptorAnnotations));
        mLogger.trace(String.format("    Stored interceptor classes: %s", interceptorClasses));

        Iterable<String> annotationNames = splitter.split(interceptorAnnotations);
        Iterable<String> classNames = splitter.split(interceptorClasses);

        for (String annotationName : annotationNames) {

            mLogger.debug(String.format("    Attempting to load annotation %s.", annotationName));

            final TypeElement typeElement = mElementUtils.getTypeElement(annotationName);

            if (typeElement != null) {

                mInterceptorAnnotationTypes.add(typeElement);
            } else {

                mLogger.debug(String.format("    Failed to load element %s.", annotationName));
            }
        }

        for (String className : classNames) {

            mLogger.debug(String.format("    Attempting to load class %s.", className));

            final TypeElement typeElement = mElementUtils.getTypeElement(className);

            if (typeElement != null) {

                mInterceptorClassTypes.add(typeElement);
            } else {

                mLogger.debug(String.format("    Failed to load element %s.", className));
            }
        }

        mInterceptorAnnotationTypesStack.addAll(mInterceptorAnnotationTypes);

        mLogger.debug("    Done loading continuation.");
    }

    /**
     * Stores continuation information in the provided continuation file.
     */
    void saveContinuation() {

        mLogger.trace("Saving continuation file.");

        if (mContinuationFile == null) {

            mLogger.debug(String.format("Continuation file not provided. Skipping continuation.", mContinuationFile));
            return;
        }

        FileWriter fileWriter = null;

        try {

            mLogger.trace("    Checking for presence of existing continuation files.");
            if (mContinuationFile.exists()) {

                boolean wasDeleted = mContinuationFile.delete();

                if (!wasDeleted) {

                    throw new IOException(String.format("Unable to delete existing continuation file '%s'",
                            mContinuationFile));
                }

                mLogger.trace("    File existed and has been successfully deleted.");
            }

            if (mContinuationFile.getParentFile() != null) {

                mLogger.trace("    Creating continuation file parent dirs.");
                mContinuationFile.getParentFile().mkdirs();
                mLogger.trace("    Done.");
            }

            Properties continuationProperties = new Properties();

            Set<String> interceptorAnnotationNames = new HashSet<String>();
            Set<String> interceptorClassNames = new HashSet<String>();

            for (Element indexedTypeName : mInterceptorAnnotationTypes) {

                interceptorAnnotationNames.add(indexedTypeName.toString());
            }

            for (Element indexedTypeName : mInterceptorClassTypes) {

                interceptorClassNames.add(indexedTypeName.toString());
            }

            Joiner joiner = Joiner.on(",").skipNulls();

            mLogger.trace(String.format("    Interceptor annotations being saved: %s", interceptorAnnotationNames));
            mLogger.trace(String.format("    Interceptor clsses being saved: %s", interceptorClassNames));
            continuationProperties.put(INTERCEPTOR_ANNOTTIONS_PROPERTY_KEY, joiner.join(interceptorAnnotationNames));
            continuationProperties.put(INTERCEPTOR_CLASSES_PROPERTY_KEY, joiner.join(interceptorClassNames));

            fileWriter = new FileWriter(mContinuationFile);
            continuationProperties.store(fileWriter, "");
        } catch (Exception e) {

            mLogger.error(String.format("    Error processing continuation index file %s'", mContinuationFile));
            throw new AnnotationProcessorException(e);
        } finally {
            FileUtils.close(fileWriter);
        }

        mLogger.trace("Done saving continuation file.");
    }

    /**
     * Adds a {@link TypeElement} to be stored in the continuation file as annotation interceptors.
     * 
     * @param element
     *            The {@link TypeElement} interceptor to add to the continuation.
     */
    void addInterceptorAnnotation(TypeElement element) {

        mInterceptorAnnotationTypes.add(element);
    }

    /**
     * Adds a set {@link TypeElement} to be stored in the continuation file as class interceptors.
     * 
     * @param interceptorClassSet
     *            The set of {@link TypeElement} interceptors to add to the continuation.
     */
    void addInterceptorClasses(Set<TypeElement> interceptorClassSet) {

        mInterceptorClassTypes.addAll(interceptorClassSet);
    }

    /**
     * Pops a interceptor annotation type from the continuation compilation. Types processes by the annotation processor
     * on a round should be removed from the continuation so they are not re-processed in the next round.
     * 
     * @param interceptorAnnotation
     *            The element to remove from the continuation compilation.
     */
    void popInterceptorAnnotation(TypeElement interceptorAnnotation) {

        mInterceptorAnnotationTypesStack.remove(interceptorAnnotation);
    }

    /**
     * Gets the set of interceptor annotation to process this round.
     * 
     * @param roundEnv
     *            The round environment for the round.
     * 
     * @return The set of interceptor annotation to process this round.
     */

    Set<TypeElement> getInterceptorAnnotationsForRound(RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ProviderInterceptorPoint.class);

        for (Element element : elements) {

            /*
             * Do not assume that because the @ProviderInterceptorPoint annotation can only be applied to annotation
             * types, only TypeElements will be returned. Compilation errors on a class can let the compiler think the
             * annotation is applied to other elements even if it is correctly applied to a class, causing a class cast
             * exception in the for loop below.
             */
            if (element instanceof TypeElement) {

                mInterceptorAnnotationTypesStack.add((TypeElement) element);
            }
        }

        return mInterceptorAnnotationTypesStack;
    }

    /**
     * Get elements from this round or the continuation environment for the provided annotation.
     * 
     * @param annotationType
     *            The annotatoin to check.
     * @param roundEnv
     *            The rounding environment to check.
     * @return The coalesced set of annotated types from the round environment + the continuation file.
     */
    @SuppressWarnings("unchecked")
    Set<? extends Element> getElementsAnotatedWith(TypeElement annotationType, RoundEnvironment roundEnv) {

        Set<TypeElement> annotatedElements = new HashSet<TypeElement>();
        annotatedElements.addAll((Set<TypeElement>) roundEnv.getElementsAnnotatedWith(annotationType));

        mLogger.trace(String.format("    Elements annotated with %s from the round environment: %s", annotationType,
                annotatedElements));

        for (TypeElement continuationInterceptorClass : mInterceptorClassTypes) {

            List<? extends AnnotationMirror> mirrors = continuationInterceptorClass.getAnnotationMirrors();

            for (AnnotationMirror mirror : mirrors) {

                if (mTypeUtils.asElement(mirror.getAnnotationType()).equals(annotationType)) {
                    mLogger.trace(String.format(
                            "        Added class %s from continuation to the list of annotated elements",
                            continuationInterceptorClass));
                    annotatedElements.add(continuationInterceptorClass);
                }
            }
        }

        mLogger.trace(String.format("    Final set of annotated elements for annotation %s: %s", annotationType,
                annotatedElements));
        return annotatedElements;
    }
}
