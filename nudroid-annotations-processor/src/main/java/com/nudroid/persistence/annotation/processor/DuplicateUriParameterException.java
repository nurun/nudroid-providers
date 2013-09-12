/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class DuplicateUriParameterException extends RuntimeException {

    private static final long serialVersionUID = -3066482963147107085L;
    private String duplicatePosition;
    private String existingPosition;
    private String paramName;

    /**
     * @param paramName
     * @param existingPosition
     * @param duplicatePosition
     */
    public DuplicateUriParameterException(String paramName, String existingPosition, String duplicatePosition) {
        
        this.paramName = paramName;
        this.existingPosition = existingPosition;
        this.duplicatePosition = duplicatePosition;
    }

    public String getDuplicatePosition() {

        return duplicatePosition;
    }

    public String getExistingPosition() {
        
        return existingPosition;
    }

    public String getParamName() {
        
        return paramName;
    }
}
