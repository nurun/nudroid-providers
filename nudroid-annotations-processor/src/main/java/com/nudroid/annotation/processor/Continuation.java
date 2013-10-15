package com.nudroid.annotation.processor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
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

/**
 * Manages continuation information. On modern IDEs, compilation can be incremental (i.e. only the modified classes are
 * compiled on a round). Since the processor requires metadata extracted from other source files, which might not be
 * included in a particular compilation round on an IDE, not all information might be available. This continuation
 * utility class manages a store of processed elements from past compilations.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Continuation {

	private static final String INTERCEPTOR_ANNOTTIONS_PROPERTY_KEY = "com.nudroid.annotation.processor.continuation.interceptor.annotations";
	private static final String INTERCEPTOR_CLASSES_PROPERTY_KEY = "com.nudroid.annotation.processor.continuation.interceptor.classes";
	private File mFile;
	private Set<TypeElement> mInterceptorAnnotationTypes = new HashSet<TypeElement>();
	private Set<TypeElement> mInterceptorClassTypes = new HashSet<TypeElement>();
	private LoggingUtils mLogger;
	private Elements mElementUtils;
	private Types mTypeUtils;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param continuationFile
	 *            The path for the continuation file.
	 */
	public Continuation(ProcessorContext processorContext, String continuationFile) {

		if (continuationFile != null) {
			this.mFile = new File(continuationFile);
		}

		this.mLogger = processorContext.logger;
		this.mElementUtils = processorContext.elementUtils;
		this.mTypeUtils = processorContext.typeUtils;
	}

	/**
	 * Loads continuation information from the provided continuation file.
	 */
	public void loadContinuation() {

		if (mFile == null) {

			mLogger.debug(String.format("    Continuation file not ptovided. Skipping continuation.", mFile));
			return;
		}

		if (!mFile.exists()) {

			mLogger.debug(String.format("    Continuation file not found. First compilation interation.", mFile));
			return;
		}

		mLogger.debug(String.format("    Continuation file found. Loading continuation information.", mFile));

		Properties continuationProperties = new Properties();

		try {
			continuationProperties.load(new FileReader(mFile));
		} catch (IOException e) {
			throw new IllegalStateException(String.format("Error while loading continuation file '%s'.", mFile), e);
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

		mLogger.debug("    Done loading continuation.");
	}

	/**
	 * Stores continuation information in the provided continuation file.
	 */
	public void saveContinuation() {

		if (mFile == null) {

			mLogger.debug(String.format("    Continuation file not ptovided. Skipping continuation.", mFile));
			return;
		}

		try {

			mLogger.trace("    Checking for presence of existing continuation files.");
			if (mFile.exists()) {

				boolean wasDeleted = mFile.delete();

				if (!wasDeleted) {

					throw new IOException(String.format("Unable to delete existing continuation file '%s'", mFile));
				}

				mLogger.trace("    File existed and has been successfully deleted.");
			}

			if (mFile.getParentFile() != null) {

				mLogger.trace("    Creating continuation file parent dirs.");
				mFile.getParentFile().mkdirs();
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

			continuationProperties.store(new FileWriter(mFile), "");
		} catch (Exception e) {

			mLogger.error(String.format("    Error processing continuation index file %s'", mFile));
			throw new AnnotationProcessorException(e);
		}
	}

	/**
	 * Adds a {@link TypeElement} to be stored in the continuation file as annotation interceptors.
	 * 
	 * @param element
	 *            The {@link TypeElement} interceptor to add to the continuation.
	 */
	public void addInterceptorAnnotation(TypeElement element) {

		mInterceptorAnnotationTypes.add(element);
	}

	/**
	 * Adds a set {@link TypeElement} to be stored in the continuation file as class interceptors.
	 * 
	 * @param interceptorClassSet
	 *            The set of {@link TypeElement} interceptors to add to the continuation.
	 */
	public void addInterceptorClasses(Set<TypeElement> interceptorClassSet) {

		mInterceptorClassTypes.addAll(interceptorClassSet);
	}

	/**
	 * Gets the interceptor annotations stored in the continuation file.
	 * 
	 * @return The interceptor annotations stored in the continuation file.
	 */
	public Set<TypeElement> getInterceptorAnnotations() {

		return Collections.unmodifiableSet(mInterceptorAnnotationTypes);
	}

	/**
	 * Gets the interceptor annotations stored in the continuation file.
	 * 
	 * @return The interceptor annotations stored in the continuation file.
	 */
	public Set<TypeElement> getInterceptorClasses() {

		return Collections.unmodifiableSet(mInterceptorClassTypes);
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
	public Set<? extends Element> getElementsAnotatedWith(TypeElement annotationType, RoundEnvironment roundEnv) {

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
