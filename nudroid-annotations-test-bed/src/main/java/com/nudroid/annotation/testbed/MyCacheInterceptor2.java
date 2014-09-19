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

package com.nudroid.annotation.testbed;

import com.nudroid.annotation.provider.delegate.intercept.InterceptorPointcut;
import com.nudroid.provider.interceptor.ContentProviderContext;
import com.nudroid.provider.interceptor.ContentProviderInterceptorAdapter;

/**
 * Cache validation interceptor for the cntent service.
 */
public class MyCacheInterceptor2 extends ContentProviderInterceptorAdapter {

    public static enum SampleEnum {
        ENUM_1, ENUM_2
    }

    /**
     * Creates an instance of this class.
     */
    public MyCacheInterceptor2(Interceptor annotation) {

    }

    @Override
    public void onCreate(ContentProviderContext context) {

    }

    /**
     * The interceptor annotation to be applied to methods.
     */
    @InterceptorPointcut
    public @interface Interceptor {

        /**
         * The remote url to call to fetch up to date data.
         */
        String sValue() default "default string";
        String[] sValues() default {"default array element 1", "default array element 2"};

        byte bValue();
        byte[] bValues();

        char cValue();
        char[] cValues();

        int iValue();
        int[] iValues();

        long lValue();
        long[] lValues();

        float fValue();
        float[] fValues();

        double dValue();
        double[] dValues();

        Class<?> zValue();
        Class<?>[] zValues();

        SampleEnum eValue();
        SampleEnum[] eValues();
    }
}
