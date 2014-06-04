/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
            if (index < 0) index = ~index;

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
