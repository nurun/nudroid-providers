package com.nudroid.annotation.processor;

import javax.lang.model.element.ExecutableElement;

/**
 * Exception raised when the URI provided to the delegate method is duplicated.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DuplicatePathException extends IllegalUriPathException {

	private static final long serialVersionUID = -4364782083955709261L;
	private ExecutableElement mOriginalMethod;

	/**
	 * @param existingDelegateMethod
	 *            The delegate method which already define the path.
	 */
	public DuplicatePathException(ExecutableElement existingDelegateMethod, String pathAndQuery) {
		super(String.format("An equivalent path has already been registerd by method %s", existingDelegateMethod));

		this.mOriginalMethod = existingDelegateMethod;
	}

	/**
	 * Gets the method which originally registered the offending path.
	 * 
	 * @return The method which originally registered the offending path.
	 */
	public Object getOriginalMethod() {

		return mOriginalMethod;
	}
}
