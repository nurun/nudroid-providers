/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class DuplicateUriPlaceholderException extends RuntimeException {

    private static final long serialVersionUID = -3066482963147107085L;
    private String duplicatePosition;
    private String existingPosition;
    private String placeholderName;

    /**
     * @param placeholderName
     * @param existingPosition
     * @param duplicatePosition
     */
    public DuplicateUriPlaceholderException(String placeholderName, String existingPosition, String duplicatePosition) {
        
        this.placeholderName = placeholderName;
        this.existingPosition = existingPosition;
        this.duplicatePosition = duplicatePosition;
    }

    public String getDuplicatePosition() {

        return duplicatePosition;
    }

    public String getExistingPosition() {
        
        return existingPosition;
    }

    public String getPlaceholderName() {
        
        return placeholderName;
    }
}
