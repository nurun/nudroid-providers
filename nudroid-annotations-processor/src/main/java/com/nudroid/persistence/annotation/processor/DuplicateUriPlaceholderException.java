package com.nudroid.persistence.annotation.processor;

/**
 * Exception raised when the same placeholder name is used more than one on a URI path and query string combination.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DuplicateUriPlaceholderException extends IllegalUriPathException {

    private static final long serialVersionUID = -3066482963147107085L;
    private String duplicatePosition;
    private String existingPosition;
    private String placeholderName;

    /**
     * Creates an instance of this class.
     * 
     * @param placeholderName
     *            The name of the placeholder in violation.
     * @param existingPosition
     *            Where in the URI this placeholder name already appears.
     * @param duplicatePosition
     *            THe position where the offending placeholder appears.
     */
    public DuplicateUriPlaceholderException(String placeholderName, String existingPosition, String duplicatePosition) {

        this.placeholderName = placeholderName;
        this.existingPosition = existingPosition;
        this.duplicatePosition = duplicatePosition;
    }

    /**
     * Gets the position of the duplicated entry.
     * 
     * @return The position of the duplicated entry.
     */
    public String getDuplicatePosition() {

        return duplicatePosition;
    }

    /**
     * Gets the position of the existing entry.
     * 
     * @return The position of the existing entry.
     */
    public String getExistingPosition() {

        return existingPosition;
    }

    /**
     * Gets the name of the placeholder in violation.
     * 
     * @return The name of the placeholder in violation.
     */
    public String getPlaceholderName() {

        return placeholderName;
    }
}
