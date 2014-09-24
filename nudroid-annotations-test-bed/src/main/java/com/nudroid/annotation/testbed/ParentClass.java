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

import android.content.Context;
import android.database.Cursor;

import com.nudroid.annotation.provider.delegate.ContentProvider;
import com.nudroid.annotation.provider.delegate.Query;
import com.nudroid.annotation.provider.delegate.PathParam;
import com.nudroid.annotation.provider.delegate.QueryParam;
import com.nudroid.provider.delegate.ContentProviderDelegate;

public class ParentClass {
    /**
     * A sample delegate to test the annotation processor.
     */
    @ContentProvider(authority = "com.nudroid.samples")
    public static class SampleContentProviderDelegate implements ContentProviderDelegate {

        @Query("/channels/test")
        public Cursor channels() {

            //... perform query
            return null;
        }

        @MyCacheInterceptor2.Interceptor
        //        @MyCacheInterceptor.Interceptor("yahoo")
        @Query("/terminals/cast/{obaba}")
        public Cursor terminals(@PathParam("obaba") long theObaba, @QueryParam("obaba2") String anotherObaba) {

            //... perform query
            return null;
        }

        @Override
        public boolean onCreate(Context context) {
            return false;
        }
    }

}

