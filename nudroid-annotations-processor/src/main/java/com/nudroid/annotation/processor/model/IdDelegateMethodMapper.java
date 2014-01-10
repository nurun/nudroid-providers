package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A mapping between a URI id and the set of delegate methods mapped to that id.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class IdDelegateMethodMapper {

    private int mUriId;

    private List<DelegateUri> mDelegateUris = new ArrayList<DelegateUri>() {
        private static final long serialVersionUID = 1L;

        /*
         * This method reorders the list by query string parameter count, descending.
         */
        public boolean add(DelegateUri delegateMethod) {
            int index = Collections.binarySearch(this, delegateMethod, new Comparator<DelegateUri>() {

                @Override
                public int compare(DelegateUri d1, DelegateUri d2) {

                    return d2.getQueryStringParameterCount() - d1.getQueryStringParameterCount();
                }
            });

            // This is equivalent to
            // if (index < 0), index = Math.abs(index) + 1;
            if (index < 0)
                index = ~index;

            super.add(index, delegateMethod);
            return true;
        }
    };
    
//    private List<DelegateMethod> mDelegateMethods = new ArrayList<DelegateMethod>() {
//        private static final long serialVersionUID = 1L;
//
//        /*
//         * This method reorders the list by query string parameter count, descending.
//         */
//        public boolean add(DelegateMethod delegateMethod) {
//            int index = Collections.binarySearch(this, delegateMethod, new Comparator<DelegateMethod>() {
//
//                @Override
//                public int compare(DelegateMethod dm1, DelegateMethod dm2) {
//
//                    return dm2.getQueryStringParameterCount() - dm1.getQueryStringParameterCount();
//                }
//            });
//
//            // This is equivalent to
//            // if (index < 0), index = Math.abs(index) + 1;
//            if (index < 0)
//                index = ~index;
//
//            super.add(index, delegateMethod);
//            return true;
//        }
//    };

    private boolean mHasQueryMethodsOnly = true;

    public IdDelegateMethodMapper(int mUriId) {

        this.mUriId = mUriId;
    }

    /**
     * Gets the URI ID.
     * 
     * @return The URI ID.
     */
    public int getUriId() {

        return mUriId;
    }

    /**
     * Checks if only Query methods are registered
     * 
     * @return <tt>true</tt> if the only query identified methods are mapped to this URI, <tt>false</tt> otherwise.
     */
    public boolean hasQueryMethodsOnly() {

        return mHasQueryMethodsOnly;
    }

    /**
     * Maps a delegate method to this URI.
     * 
     * @param method
     *            The delegate method to add.
     * 
     * @return
     */
    public boolean add(DelegateMethod method) {

        if (method.getQueryStringParameterCount() == 0) {

            mHasQueryMethodsOnly = false;
        }

        return mDelegateMethods.add(method);
    }

    /**
     * Gets the methods registered for this URI.
     * 
     * @return The methods registered for this URI.
     */
    public List<DelegateMethod> getDelegateMethods() {

        return mDelegateMethods;
    }
}
