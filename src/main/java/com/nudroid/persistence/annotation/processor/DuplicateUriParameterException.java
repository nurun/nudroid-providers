/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class DuplicateUriParameterException extends IllegalArgumentException {

    private static final long serialVersionUID = -3066482963147107085L;
    private int duplicatePosition;
    private int existingPosition;
    private String paramName;

    /**
     * @param paramName
     * @param existingPosition
     * @param duplicatePosition
     */
    public DuplicateUriParameterException(String paramName, int existingPosition, int duplicatePosition) {
        
        this.paramName = paramName;
        this.existingPosition = existingPosition;
        this.duplicatePosition = duplicatePosition;
    }

    public int getDuplicatePosition() {

        return duplicatePosition;
    }

    public int getExistingPosition() {
        
        return existingPosition;
    }

    public String getParamName() {
        
        return paramName;
    }
}
