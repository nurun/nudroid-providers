/**
 * 
 */
package com.nudroid.persistence.annotation.processor;

/**
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 * 
 */
public class UriPlaceholderParameter {

    private String position;
    private String name;

    /**
     * @param name
     * @param position
     */
    public UriPlaceholderParameter(String name, int position) {
        super();
        this.name = name;
        this.position = Integer.toString(position);
    }

    public UriPlaceholderParameter(String name, String queryParameterName) {
        super();
        this.position = queryParameterName;
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UriPlaceholderParameter other = (UriPlaceholderParameter) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "UriPlaceholderParameter [position=" + position + ", name=" + name + "]";
    }
}
