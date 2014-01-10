package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UriIdDelegateMethodMapper {

    private int mUriId;
    private List<DelegateMethod> mDelegateMethods = new ArrayList<DelegateMethod>() {
        private static final long serialVersionUID = 1L;

        /*
         * This method reorders the list by query string parameter count, descending. 
         */
        public boolean add(DelegateMethod delegateMethod) {
            int index = Collections.binarySearch(this, delegateMethod, new Comparator<DelegateMethod>() {

                @Override
                public int compare(DelegateMethod dm1, DelegateMethod dm2) {

                    return dm2.getQueryStringParameterCount() - dm1.getQueryStringParameterCount();
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

    private boolean mHasQueryMethodsOnly = true;

    public UriIdDelegateMethodMapper(int mUriId) {

        this.mUriId = mUriId;
    }

    public int getUriId() {

        return mUriId;
    }

    public boolean hasQueryMethodsOnly() {

        return mHasQueryMethodsOnly;
    }

    public boolean add(DelegateMethod e) {

        if (e.getQueryStringParameterCount() == 0) {
            
            mHasQueryMethodsOnly = false;
        }

        return mDelegateMethods.add(e);
    }

    public List<DelegateMethod> getDelegateMethods() {
        return mDelegateMethods;
    }
}
