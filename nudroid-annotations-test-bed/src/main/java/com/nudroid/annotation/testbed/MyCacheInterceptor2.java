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

import java.util.Collection;

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

        byte bValue() default 3;

        byte[] bValues() default {44, 55};

        char cValue() default 'c';

        char[] cValues() default {'d', 'e'};

        int iValue() default 300;

        int[] iValues() default {444, 555};

        long lValue() default 1000;

        long[] lValues() default {5000, 6666};

        float fValue() default 0.0f;

        float[] fValues() default {2.89f, 2.00009f};

        double dValue() default -1000.0;

        double[] dValues() default {1.999999999999, 1.0000000007};

        Class<?> zValue() default String.class;

        Class<?>[] zValues() default {Object.class, Collection.class};

        SampleEnum eValue() default SampleEnum.ENUM_1;

        SampleEnum[] eValues() default {SampleEnum.ENUM_1, SampleEnum.ENUM_1, SampleEnum.ENUM_2};
    }
}
