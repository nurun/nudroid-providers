package com.nudroid.annotation.processor.model;

/**
 * Represents a content provider authority.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class Authority {

    private String mName;
    private DelegateClass delegateClass;

    /**
     * Creates an authority representation.
     * 
     * @param authorityName
     *            The authority name.
     * @param delegateClass
     */
    public Authority(String authorityName, DelegateClass delegateClass) {

        this.mName = authorityName;
        this.delegateClass = delegateClass;
    }

    /**
     * Gets the authority name.
     * 
     * @return The authority name.
     */
    public String getName() {

        return mName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Authority '" + mName + "'";
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Authority other = (Authority) obj;
        if (mName == null) {
            if (other.mName != null) return false;
        } else if (!mName.equals(other.mName)) return false;
        return true;
    }
}
