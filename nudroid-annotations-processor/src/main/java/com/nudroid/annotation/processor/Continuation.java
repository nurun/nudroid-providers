/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.nudroid.annotation.processor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Update continuation to all processed types. <br/>
 * Manages continuation of incremental compilation. On modern IDEs, compilation can be incremental (i.e. only the
 * modified classes are compiled on a round). Since the processor requires metadata extracted from other source files,
 * which might not be included in a particular compilation round on an IDE, not all information might be available to
 * Nudroid's annotation processor. This continuation utility class manages a store of processed elements from past
 * compilations.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
class Continuation {

    private File mContinuationFile;
    private LoggingUtils mLogger;
    private Elements mElementUtils;

    private Set<TypeElement> mContinuationTypes = new HashSet<TypeElement>();
    private Set<TypeElement> mContinuationTypesStack = new HashSet<TypeElement>();

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

        Scanner scanner = null;

        try {

            scanner = new Scanner(mContinuationFile);

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
                final TypeElement typeElement = mElementUtils.getTypeElement(line);

                if (typeElement != null) {

                    mLogger.trace(String.format("    Type %s was found and added to continuation configuration.", line));
                    mContinuationTypes.add(typeElement);
                    mContinuationTypesStack.add(typeElement);
                } else {

                    mLogger.trace(String.format("    Type not found: %s", line));
                }
            }
        } catch (Exception e) {

            mLogger.error(String.format("    Error loading continuation index file %s'", mContinuationFile));
            throw new AnnotationProcessorException(e);
        } finally {

            FileUtils.close(scanner);
        }
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

        PrintWriter writer = null;

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

            writer = new PrintWriter(mContinuationFile);

            mLogger.trace(String.format("    Continuation types being saved: %s", mContinuationTypes));

            for (Element indexedTypeName : mContinuationTypes) {

                writer.println(indexedTypeName.toString());
            }

            FileUtils.close(writer);

        } catch (Exception e) {

            mLogger.error(String.format("    Error saving continuation index file %s'", mContinuationFile));
            throw new AnnotationProcessorException(e);
        } finally {
            FileUtils.close(writer);
        }

        mLogger.trace("Done saving continuation file.");
    }

    /**
     * Adds a {@link TypeElement} to this continuation environment.
     * 
     * @param typeElement
     *            The type element to add.
     */
    void addTypeToContinuation(TypeElement typeElement) {

        mContinuationTypes.add(typeElement);
        mContinuationTypesStack.add(typeElement);
    }

    /**
     * Looks for elements annotated with the given annotation on the {@link RoundEnvironment} as well as the
     * continuation information.
     * 
     * @param annotationClass
     *            The class type of the annotation to check.
     * @param roundEnv
     *            The {@link RoundEnvironment} for the round.
     * 
     * @return The set of elements from the round environment as well as the continuation environment annotated with the
     *         given annotation class.
     */
    Set<? extends Element> getElementsAnotatedWith(Class<? extends Annotation> annotationClass,
            RoundEnvironment roundEnv) {

        Set<Element> annotatedElementsForRound = new HashSet<Element>();

        annotatedElementsForRound.addAll(roundEnv.getElementsAnnotatedWith(annotationClass));

        mLogger.trace(String.format("    Searching for elements annotated with %s", annotationClass.getName()));

        Set<String> roundEnvironmentRootElementsName = new HashSet<String>();

        for (Element e : roundEnv.getRootElements()) {
            roundEnvironmentRootElementsName.add(e.toString());
        }

        for (Element continuationClass : mContinuationTypesStack) {

            Element rootCheck = continuationClass;

            boolean isInRootElements = false;

            for (; rootCheck != null; rootCheck = rootCheck.getEnclosingElement()) {

                if (roundEnvironmentRootElementsName.contains(rootCheck.toString())) {
                    isInRootElements = true;
                    break;
                }
            }

            if (!isInRootElements) {

                Annotation annotation = continuationClass.getAnnotation(annotationClass);

                if (annotation != null) {
                    mLogger.trace(String.format("    Type %s was not part of round environment information but was"
                            + " present in the continuation configuration. Adding to set of target elements.",
                            continuationClass));
                    annotatedElementsForRound.add(continuationClass);
                }
            }
        }

        return annotatedElementsForRound;
    }

    /**
     * Clears this compilation's stack of continuation elements to process.
     */
    void flushStack() {

        mContinuationTypesStack.clear();
    }

    // private void checkForAnnotationOnElementAndChildren(Element elementToCheck,
    // Class<? extends Annotation> annotationClass, Set<Element> annotatedElementsForRound) {
    //
    // Annotation annotation = elementToCheck.getAnnotation(annotationClass);
    //
    // if (annotation != null && annotatedElementsForRound.add(elementToCheck)) {
    //
    // mLogger.trace(String.format("        Added Element %s from continuation to the list of annotated elements",
    // elementToCheck));
    // }
    //
    // List<? extends Element> enclosedElements = elementToCheck.getEnclosedElements();
    //
    // for (Element enclosedElement : enclosedElements) {
    //
    // checkForAnnotationOnElementAndChildren(enclosedElement, annotationClass, annotatedElementsForRound);
    // }
    // }
}
