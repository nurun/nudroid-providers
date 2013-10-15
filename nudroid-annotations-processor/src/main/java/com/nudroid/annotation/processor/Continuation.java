package com.nudroid.annotation.processor;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.TypeElement;

/**
 * Manages continuation information. On modern IDEs, compilation can be incremental (i.e. only the modified classes are
 * compiled on a round). Since the processor requires metadata extracted from other source files, which might not be
 * included in a particular compilation round on an IDE, not all information might be available. This continuation
 * utility class manages a store of processed elements from past compilations.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Continuation {

	private File mFile;
	private Set<TypeElement> mInterceptorAnnotationTypes = new HashSet<TypeElement>();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param continuationFile
	 *            The path for the continuation file.
	 */
	public Continuation(String continuationFile) {

		mFile = new File(continuationFile);
	}

	/**
	 * Loads continuation information from the provided continuation file.
	 */
	public void loadContinuation() {

	}

	/**
	 * Stores continuation information in the provided continuation file.
	 */
	public void saveContinuation() {

	}

	/**
	 * Adds a {@link TypeElement} to be stored in the continuation file.
	 * 
	 * @param element
	 *            The {@link TypeElement} interceptor to add to the continuation.
	 */
	public void addInterceptorAnnotation(TypeElement element) {

		mInterceptorAnnotationTypes.add(element);
	}

	/**
	 * Gets the interceptor annotations stored in the continuation file.
	 * 
	 * @return The interceptor annotations stored in the continuation file.
	 */
	public Collection<? extends TypeElement> getInterceptorAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}
}
