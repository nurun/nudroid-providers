package com.nudroid.provider.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.nudroid.annotation.provider.delegate.Delete;

@SuppressWarnings("serial")
public class ConcreteAnnotation implements Annotation, Serializable, Delete {

    private String mValue;

    public ConcreteAnnotation(String value) {
        
        this.mValue = value;
    }
    
    /**
     * <p/>
     * {@inheritDoc}
     * @see com.nudroid.annotation.provider.delegate.Delete#value()
     */
    @Override
    public String value() {
        return mValue;
    }
    
    /**
     * <p/>
     * {@inheritDoc}
     * 
     * @see java.lang.annotation.Annotation#annotationType()
     */
    public Class<? extends Annotation> annotationType() {
        return Delete.class;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {

        Method[] methods = annotationType().getDeclaredMethods();

        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other instanceof Annotation) {

            Annotation otherAnnotation = (Annotation) other;

            if (this.annotationType().equals(otherAnnotation.annotationType())) {

                for (Method method : methods) {

                    try {
                        Object otherValue = method.invoke(otherAnnotation, new Object[] {});
                        Object thisValue = method.invoke(this, new Object[] {});

                        if (!thisValue.equals(otherValue)) {

                            return false;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        Method[] methods = annotationType().getDeclaredMethods();

        int hashCode = 0;

        try {
            for (Method method : methods) {
                int name = 127 * method.getName().hashCode();

                Object object = method.invoke(this, new Object[] {});

                int value = 0;

                if (object.getClass().isArray()) {

                    Class<?> type = object.getClass().getComponentType();

                    if (type.isPrimitive()) {

                        if (Long.TYPE == type) value = Arrays.hashCode((Long[]) object);
                        else if (Integer.TYPE == type) value = Arrays.hashCode((Integer[]) object);
                        else if (Short.TYPE == type) value = Arrays.hashCode((Short[]) object);
                        else if (Double.TYPE == type) value = Arrays.hashCode((Double[]) object);
                        else if (Float.TYPE == type) value = Arrays.hashCode((Float[]) object);
                        else if (Boolean.TYPE == type) value = Arrays.hashCode((Long[]) object);
                        else if (Byte.TYPE == type) value = Arrays.hashCode((Byte[]) object);
                        else if (Character.TYPE == type) value = Arrays.hashCode((Character[]) object);
                    } else {
                        value = Arrays.hashCode((Object[]) object);
                    }
                } else {

                    value = object.hashCode();
                }

                hashCode += name ^ value;
            }
        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        return hashCode;
    }

    /**
     * <p/>
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        Method[] methods = annotationType().getDeclaredMethods();

        StringBuilder sb = new StringBuilder("@" + annotationType().getName() + "(");

        try {
            int lenght = methods.length;

            for (int i = 0; i < lenght; i++) {
                sb.append(methods[i].getName()).append("=");

                sb.append(methods[i].invoke(this, new Object[] {}));

                if (i < lenght - 1) {
                    sb.append(",");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        sb.append(")");

        return sb.toString();
    }
}
